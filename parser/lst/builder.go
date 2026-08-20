package lst

import (
	"go/ast"
	"go/token"
	"go/types"

	"github.com/google/uuid"
	"github.com/openrewrite/rewrite-go/parser/proto"
)

type Builder struct {
	fset *token.FileSet
	info *types.Info
	src  []byte
	// cursor is the byte offset just past the last token accounted for. Nodes are built
	// in source order, so everything between the cursor and the next node is exactly
	// that node's prefix. The cursor is what stops nested nodes that start at the same
	// offset from each claiming the same whitespace.
	cursor int
}

func NewBuilder(fset *token.FileSet, info *types.Info, src []byte) *Builder {
	return &Builder{
		fset: fset,
		info: info,
		src:  src,
	}
}

func (b *Builder) BuildFile(file *ast.File) *proto.GoFile {
	goFile := &proto.GoFile{
		Id:          newUUID(),
		Prefix:      b.prefix(file.Pos()),
		Markers:     &proto.Markers{Id: newUUID()},
		SourcePath:  b.fset.Position(file.Pos()).Filename,
		CharsetName: "UTF-8",
	}

	b.consume(file.Package, "package")

	if file.Name != nil {
		goFile.PackageClause = &proto.PackageClause{
			Id:      newUUID(),
			Prefix:  &proto.Space{},
			Markers: &proto.Markers{Id: newUUID()},
			Name:    b.buildIdent(file.Name),
		}
	}

	// Imports come from Decls, not file.Imports. file.Imports is flattened, so building
	// from it loses the "import ( ... )" grouping and leaves the import GenDecl to be
	// emitted a second time by the declaration loop below.
	for _, decl := range file.Decls {
		if gd, ok := decl.(*ast.GenDecl); ok && gd.Tok == token.IMPORT {
			goFile.Imports = append(goFile.Imports, b.buildImportDecl(gd))
			continue
		}
		if d := b.buildDecl(decl); d != nil {
			goFile.Declarations = append(goFile.Declarations, d)
		}
	}

	goFile.Eof = b.rest()

	return goFile
}

func (b *Builder) buildImportDecl(d *ast.GenDecl) *proto.ImportDecl {
	id := &proto.ImportDecl{
		Id:      newUUID(),
		Prefix:  b.prefix(d.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Grouped: d.Lparen.IsValid(),
	}

	b.consume(d.Pos(), "import")
	if d.Lparen.IsValid() {
		b.consume(d.Lparen, "(")
	}

	for _, spec := range d.Specs {
		is, ok := spec.(*ast.ImportSpec)
		if !ok {
			continue
		}
		out := &proto.ImportSpec{
			Id:      newUUID(),
			Prefix:  b.prefix(is.Pos()),
			Markers: &proto.Markers{Id: newUUID()},
		}
		if is.Name != nil {
			out.Alias = b.buildIdent(is.Name)
		}
		out.Path = b.buildBasicLit(is.Path)
		id.Specs = append(id.Specs, out)
	}

	if d.Rparen.IsValid() {
		id.End = b.prefix(d.Rparen)
		b.consume(d.Rparen, ")")
	}

	return id
}

func (b *Builder) buildDecl(decl ast.Decl) *proto.Decl {
	switch d := decl.(type) {
	case *ast.FuncDecl:
		return &proto.Decl{
			Decl: &proto.Decl_FuncDecl{
				FuncDecl: b.buildFuncDecl(d),
			},
		}
	case *ast.GenDecl:
		return &proto.Decl{
			Decl: &proto.Decl_GenDecl{
				GenDecl: b.buildGenDecl(d),
			},
		}
	}
	return nil
}

func (b *Builder) buildFuncDecl(d *ast.FuncDecl) *proto.FuncDecl {
	fd := &proto.FuncDecl{
		Id:      newUUID(),
		Prefix:  b.prefix(d.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	b.consume(d.Pos(), "func")

	// Fields are built in source order so the cursor stays in step: a method's receiver
	// precedes its name.
	if d.Recv != nil && len(d.Recv.List) > 0 {
		b.consumeText("(")
		fd.Recv = b.buildField(d.Recv.List[0])
		b.consumeText(")")
	}

	fd.Name = b.buildIdent(d.Name)

	if d.Type != nil {
		fd.Type = b.buildFuncType(d.Type)
	}

	if d.Body != nil {
		fd.Body = b.buildBlockStmt(d.Body)
	}

	return fd
}

func (b *Builder) buildGenDecl(d *ast.GenDecl) *proto.GenDecl {
	gd := &proto.GenDecl{
		Id:      newUUID(),
		Prefix:  b.prefix(d.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Tok:     d.Tok.String(),
		Grouped: d.Lparen.IsValid(),
	}

	b.consume(d.Pos(), d.Tok.String())
	if d.Lparen.IsValid() {
		b.consume(d.Lparen, "(")
	}

	for _, spec := range d.Specs {
		if s := b.buildSpec(spec); s != nil {
			gd.Specs = append(gd.Specs, s)
		}
	}

	if d.Rparen.IsValid() {
		gd.End = b.prefix(d.Rparen)
		b.consume(d.Rparen, ")")
	}

	return gd
}

func (b *Builder) buildSpec(spec ast.Spec) *proto.Spec {
	switch s := spec.(type) {
	case *ast.ValueSpec:
		return &proto.Spec{
			Spec: &proto.Spec_ValueSpec{
				ValueSpec: b.buildValueSpec(s),
			},
		}
	case *ast.TypeSpec:
		return &proto.Spec{
			Spec: &proto.Spec_TypeSpec{
				TypeSpec: b.buildTypeSpec(s),
			},
		}
	}
	return nil
}

func (b *Builder) buildValueSpec(s *ast.ValueSpec) *proto.ValueSpec {
	vs := &proto.ValueSpec{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	for _, name := range s.Names {
		vs.Names = append(vs.Names, b.buildIdent(name))
	}

	if s.Type != nil {
		vs.Type = b.buildExpr(s.Type)
	}

	for _, val := range s.Values {
		if e := b.buildExpr(val); e != nil {
			vs.Values = append(vs.Values, e)
		}
	}

	return vs
}

func (b *Builder) buildTypeSpec(s *ast.TypeSpec) *proto.TypeSpec {
	ts := &proto.TypeSpec{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Name:    b.buildIdent(s.Name),
		Assign:  s.Assign.IsValid(),
	}

	if s.Type != nil {
		ts.Type = b.buildExpr(s.Type)
	}

	return ts
}

func (b *Builder) buildStmt(stmt ast.Stmt) *proto.Stmt {
	switch s := stmt.(type) {
	case *ast.BlockStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_BlockStmt{
				BlockStmt: b.buildBlockStmt(s),
			},
		}
	case *ast.ExprStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_ExprStmt{
				ExprStmt: &proto.ExprStmt{
					Id:      newUUID(),
					Prefix:  b.prefix(s.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					Expr:    b.buildExpr(s.X),
				},
			},
		}
	case *ast.AssignStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_AssignStmt{
				AssignStmt: b.buildAssignStmt(s),
			},
		}
	case *ast.ReturnStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_ReturnStmt{
				ReturnStmt: b.buildReturnStmt(s),
			},
		}
	case *ast.IfStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_IfStmt{
				IfStmt: b.buildIfStmt(s),
			},
		}
	case *ast.ForStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_ForStmt{
				ForStmt: b.buildForStmt(s),
			},
		}
	case *ast.RangeStmt:
		return &proto.Stmt{
			Stmt: &proto.Stmt_RangeStmt{
				RangeStmt: b.buildRangeStmt(s),
			},
		}
	case *ast.DeclStmt:
		if d := b.buildDecl(s.Decl); d != nil {
			return &proto.Stmt{
				Stmt: &proto.Stmt_DeclStmt{
					DeclStmt: &proto.DeclStmt{
						Id:      newUUID(),
						Prefix:  b.prefix(s.Pos()),
						Markers: &proto.Markers{Id: newUUID()},
						Decl:    d,
					},
				},
			}
		}
	}
	return nil
}

func (b *Builder) buildBlockStmt(s *ast.BlockStmt) *proto.BlockStmt {
	block := &proto.BlockStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	b.consume(s.Lbrace, "{")

	for _, stmt := range s.List {
		if st := b.buildStmt(stmt); st != nil {
			block.Stmts = append(block.Stmts, st)
		}
	}

	block.End = b.prefix(s.Rbrace)
	b.consume(s.Rbrace, "}")

	return block
}

func (b *Builder) buildAssignStmt(s *ast.AssignStmt) *proto.AssignStmt {
	as := &proto.AssignStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Tok:     s.Tok.String(),
	}

	for i, lhs := range s.Lhs {
		if i > 0 {
			b.consumeText(",")
		}
		if e := b.buildExpr(lhs); e != nil {
			as.Lhs = append(as.Lhs, e)
		}
	}

	b.consume(s.TokPos, s.Tok.String())

	for i, rhs := range s.Rhs {
		if i > 0 {
			b.consumeText(",")
		}
		if e := b.buildExpr(rhs); e != nil {
			as.Rhs = append(as.Rhs, e)
		}
	}

	return as
}

func (b *Builder) buildReturnStmt(s *ast.ReturnStmt) *proto.ReturnStmt {
	rs := &proto.ReturnStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	b.consume(s.Pos(), "return")

	for i, result := range s.Results {
		if i > 0 {
			b.consumeText(",")
		}
		if e := b.buildExpr(result); e != nil {
			rs.Results = append(rs.Results, e)
		}
	}

	return rs
}

func (b *Builder) buildIfStmt(s *ast.IfStmt) *proto.IfStmt {
	is := &proto.IfStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	b.consume(s.If, "if")

	if s.Init != nil {
		is.Init = b.buildStmt(s.Init)
		b.consumeText(";")
	}

	if s.Cond != nil {
		is.Cond = b.buildExpr(s.Cond)
	}

	if s.Body != nil {
		is.Body = b.buildBlockStmt(s.Body)
	}

	if s.Else != nil {
		b.consumeText("else")
		is.ElseStmt = b.buildStmt(s.Else)
	}

	return is
}

func (b *Builder) buildForStmt(s *ast.ForStmt) *proto.ForStmt {
	fs := &proto.ForStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if s.Init != nil {
		fs.Init = b.buildStmt(s.Init)
	}

	if s.Cond != nil {
		fs.Cond = b.buildExpr(s.Cond)
	}

	if s.Post != nil {
		fs.Post = b.buildStmt(s.Post)
	}

	if s.Body != nil {
		fs.Body = b.buildBlockStmt(s.Body)
	}

	return fs
}

func (b *Builder) buildRangeStmt(s *ast.RangeStmt) *proto.RangeStmt {
	rs := &proto.RangeStmt{
		Id:      newUUID(),
		Prefix:  b.prefix(s.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Tok:     s.Tok.String(),
	}

	if s.Key != nil {
		rs.Key = b.buildExpr(s.Key)
	}

	if s.Value != nil {
		rs.Value = b.buildExpr(s.Value)
	}

	if s.X != nil {
		rs.X = b.buildExpr(s.X)
	}

	if s.Body != nil {
		rs.Body = b.buildBlockStmt(s.Body)
	}

	return rs
}

func (b *Builder) buildExpr(expr ast.Expr) *proto.Expr {
	switch e := expr.(type) {
	case *ast.Ident:
		return &proto.Expr{
			Expr: &proto.Expr_Ident{
				Ident: b.buildIdent(e),
			},
		}
	case *ast.BasicLit:
		return &proto.Expr{
			Expr: &proto.Expr_BasicLit{
				BasicLit: b.buildBasicLit(e),
			},
		}
	case *ast.CallExpr:
		return &proto.Expr{
			Expr: &proto.Expr_CallExpr{
				CallExpr: b.buildCallExpr(e),
			},
		}
	case *ast.SelectorExpr:
		return &proto.Expr{
			Expr: &proto.Expr_SelectorExpr{
				SelectorExpr: b.buildSelectorExpr(e),
			},
		}
	case *ast.BinaryExpr:
		return &proto.Expr{
			Expr: &proto.Expr_BinaryExpr{
				BinaryExpr: b.buildBinaryExpr(e),
			},
		}
	case *ast.UnaryExpr:
		return &proto.Expr{
			Expr: &proto.Expr_UnaryExpr{
				UnaryExpr: b.buildUnaryExpr(e),
			},
		}
	case *ast.ParenExpr:
		return &proto.Expr{
			Expr: &proto.Expr_ParenExpr{
				ParenExpr: &proto.ParenExpr{
					Id:      newUUID(),
					Prefix:  b.prefix(e.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					X:       b.buildExpr(e.X),
				},
			},
		}
	case *ast.CompositeLit:
		return &proto.Expr{
			Expr: &proto.Expr_CompositeLit{
				CompositeLit: b.buildCompositeLit(e),
			},
		}
	case *ast.FuncLit:
		return &proto.Expr{
			Expr: &proto.Expr_FuncLit{
				FuncLit: &proto.FuncLit{
					Id:      newUUID(),
					Prefix:  b.prefix(e.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					Type:    b.buildFuncType(e.Type),
					Body:    b.buildBlockStmt(e.Body),
				},
			},
		}
	case *ast.IndexExpr:
		return &proto.Expr{
			Expr: &proto.Expr_IndexExpr{
				IndexExpr: &proto.IndexExpr{
					Id:      newUUID(),
					Prefix:  b.prefix(e.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					X:       b.buildExpr(e.X),
					Index:   b.buildExpr(e.Index),
				},
			},
		}
	case *ast.StarExpr:
		return &proto.Expr{
			Expr: &proto.Expr_StarExpr{
				StarExpr: &proto.StarExpr{
					Id:      newUUID(),
					Prefix:  b.prefix(e.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					X:       b.buildExpr(e.X),
				},
			},
		}
	case *ast.KeyValueExpr:
		return &proto.Expr{
			Expr: &proto.Expr_KeyValueExpr{
				KeyValueExpr: &proto.KeyValueExpr{
					Id:      newUUID(),
					Prefix:  b.prefix(e.Pos()),
					Markers: &proto.Markers{Id: newUUID()},
					Key:     b.buildExpr(e.Key),
					Value:   b.buildExpr(e.Value),
				},
			},
		}
	case *ast.InterfaceType:
		return &proto.Expr{
			Expr: &proto.Expr_InterfaceTypeExpr{
				InterfaceTypeExpr: b.buildInterfaceTypeExpr(e),
			},
		}
	case *ast.ArrayType:
		return &proto.Expr{
			Expr: &proto.Expr_ArrayTypeExpr{
				ArrayTypeExpr: b.buildArrayTypeExpr(e),
			},
		}
	case *ast.MapType:
		return &proto.Expr{
			Expr: &proto.Expr_MapTypeExpr{
				MapTypeExpr: b.buildMapTypeExpr(e),
			},
		}
	case *ast.ChanType:
		return &proto.Expr{
			Expr: &proto.Expr_ChanTypeExpr{
				ChanTypeExpr: b.buildChanTypeExpr(e),
			},
		}
	case *ast.StructType:
		return &proto.Expr{
			Expr: &proto.Expr_StructTypeExpr{
				StructTypeExpr: b.buildStructTypeExpr(e),
			},
		}
	case *ast.FuncType:
		return &proto.Expr{
			Expr: &proto.Expr_FuncTypeExpr{
				FuncTypeExpr: b.buildFuncTypeExpr(e),
			},
		}
	case *ast.TypeAssertExpr:
		return &proto.Expr{
			Expr: &proto.Expr_TypeAssertExpr{
				TypeAssertExpr: b.buildTypeAssertExpr(e),
			},
		}
	}
	return nil
}

func (b *Builder) buildInterfaceTypeExpr(it *ast.InterfaceType) *proto.InterfaceTypeExpr {
	ite := &proto.InterfaceTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(it.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if it.Methods != nil {
		for _, method := range it.Methods.List {
			ite.Methods = append(ite.Methods, b.buildMethod(method))
		}
	}

	return ite
}

func (b *Builder) buildMethod(field *ast.Field) *proto.Method {
	method := &proto.Method{
		Id:      newUUID(),
		Prefix:  b.prefix(field.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if len(field.Names) > 0 {
		method.Name = field.Names[0].Name
	}

	if field.Type != nil {
		if ft, ok := field.Type.(*ast.FuncType); ok {
			method.Type = b.buildFuncType(ft)
		}
	}

	return method
}

func (b *Builder) buildArrayTypeExpr(at *ast.ArrayType) *proto.ArrayTypeExpr {
	ate := &proto.ArrayTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(at.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if at.Len != nil {
		ate.Len = b.buildExpr(at.Len)
	}

	if at.Elt != nil {
		ate.Elt = b.buildExpr(at.Elt)
	}

	return ate
}

func (b *Builder) buildMapTypeExpr(mt *ast.MapType) *proto.MapTypeExpr {
	mte := &proto.MapTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(mt.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if mt.Key != nil {
		mte.Key = b.buildExpr(mt.Key)
	}

	if mt.Value != nil {
		mte.Value = b.buildExpr(mt.Value)
	}

	return mte
}

func (b *Builder) buildChanTypeExpr(ct *ast.ChanType) *proto.ChanTypeExpr {
	cte := &proto.ChanTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(ct.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Dir:     int32(ct.Dir),
	}

	if ct.Value != nil {
		cte.Value = b.buildExpr(ct.Value)
	}

	return cte
}

func (b *Builder) buildStructTypeExpr(st *ast.StructType) *proto.StructTypeExpr {
	ste := &proto.StructTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(st.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	b.consume(st.Pos(), "struct")
	if st.Fields != nil {
		b.consumeText("{")
		for _, field := range st.Fields.List {
			ste.Fields = append(ste.Fields, b.buildField(field))
		}
		b.consumeText("}")
	}

	return ste
}

func (b *Builder) buildFuncTypeExpr(ft *ast.FuncType) *proto.FuncTypeExpr {
	fte := &proto.FuncTypeExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(ft.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if ft.Params != nil {
		for _, param := range ft.Params.List {
			fte.Params = append(fte.Params, b.buildField(param))
		}
	}

	if ft.Results != nil {
		for _, result := range ft.Results.List {
			fte.Results = append(fte.Results, b.buildField(result))
		}
	}

	return fte
}

func (b *Builder) buildTypeAssertExpr(ta *ast.TypeAssertExpr) *proto.TypeAssertExpr {
	tae := &proto.TypeAssertExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(ta.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if ta.X != nil {
		tae.X = b.buildExpr(ta.X)
	}

	if ta.Type != nil {
		tae.Type = b.buildExpr(ta.Type)
	}

	if tv, ok := b.info.Types[ta]; ok {
		tae.ResolvedType = b.convertType(tv.Type)
	}

	return tae
}

func (b *Builder) buildIdent(id *ast.Ident) *proto.Ident {
	ident := &proto.Ident{
		Id:      newUUID(),
		Prefix:  b.prefix(id.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Name:    id.Name,
	}
	b.consume(id.Pos(), id.Name)

	if tv, ok := b.info.Types[id]; ok {
		ident.Type = b.convertType(tv.Type)
	}

	return ident
}

func (b *Builder) buildBasicLit(lit *ast.BasicLit) *proto.BasicLit {
	bl := &proto.BasicLit{
		Id:      newUUID(),
		Prefix:  b.prefix(lit.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Kind:    lit.Kind.String(),
		Value:   lit.Value,
	}
	b.consume(lit.Pos(), lit.Value)
	return bl
}

func (b *Builder) buildCallExpr(call *ast.CallExpr) *proto.CallExpr {
	ce := &proto.CallExpr{
		Id:       newUUID(),
		Prefix:   b.prefix(call.Pos()),
		Markers:  &proto.Markers{Id: newUUID()},
		Fun:      b.buildExpr(call.Fun),
		Ellipsis: call.Ellipsis.IsValid(),
	}

	b.consume(call.Lparen, "(")

	for i, arg := range call.Args {
		if i > 0 {
			b.consumeText(",")
		}
		if e := b.buildExpr(arg); e != nil {
			ce.Args = append(ce.Args, e)
		}
	}

	b.consume(call.Rparen, ")")

	if tv, ok := b.info.Types[call]; ok {
		ce.Type = b.convertType(tv.Type)
	}

	return ce
}

func (b *Builder) buildSelectorExpr(sel *ast.SelectorExpr) *proto.SelectorExpr {
	se := &proto.SelectorExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(sel.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}
	se.X = b.buildExpr(sel.X)
	b.consumeText(".")
	se.Sel = b.buildIdent(sel.Sel)

	if tv, ok := b.info.Types[sel]; ok {
		se.Type = b.convertType(tv.Type)
	}

	return se
}

func (b *Builder) buildBinaryExpr(bin *ast.BinaryExpr) *proto.BinaryExpr {
	be := &proto.BinaryExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(bin.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Op:      bin.Op.String(),
	}
	be.X = b.buildExpr(bin.X)
	b.consume(bin.OpPos, bin.Op.String())
	be.Y = b.buildExpr(bin.Y)

	if tv, ok := b.info.Types[bin]; ok {
		be.Type = b.convertType(tv.Type)
	}

	return be
}

func (b *Builder) buildUnaryExpr(un *ast.UnaryExpr) *proto.UnaryExpr {
	ue := &proto.UnaryExpr{
		Id:      newUUID(),
		Prefix:  b.prefix(un.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
		Op:      un.Op.String(),
		X:       b.buildExpr(un.X),
	}

	if tv, ok := b.info.Types[un]; ok {
		ue.Type = b.convertType(tv.Type)
	}

	return ue
}

func (b *Builder) buildCompositeLit(lit *ast.CompositeLit) *proto.CompositeLit {
	cl := &proto.CompositeLit{
		Id:      newUUID(),
		Prefix:  b.prefix(lit.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	if lit.Type != nil {
		cl.Type = b.buildExpr(lit.Type)
	}

	for _, elt := range lit.Elts {
		if e := b.buildExpr(elt); e != nil {
			cl.Elts = append(cl.Elts, e)
		}
	}

	if tv, ok := b.info.Types[lit]; ok {
		cl.ResolvedType = b.convertType(tv.Type)
	}

	return cl
}

func (b *Builder) buildFuncType(ft *ast.FuncType) *proto.FuncType {
	funcType := &proto.FuncType{}

	if ft.Params != nil {
		b.consume(ft.Params.Opening, "(")
		for i, field := range ft.Params.List {
			if i > 0 {
				b.consumeText(",")
			}
			funcType.Params = append(funcType.Params, b.buildField(field))
		}
		b.consume(ft.Params.Closing, ")")
	}

	if ft.Results != nil {
		if ft.Results.Opening.IsValid() {
			b.consume(ft.Results.Opening, "(")
		}
		for i, field := range ft.Results.List {
			if i > 0 {
				b.consumeText(",")
			}
			funcType.Results = append(funcType.Results, b.buildField(field))
		}
		if ft.Results.Closing.IsValid() {
			b.consume(ft.Results.Closing, ")")
		}
	}

	return funcType
}

func (b *Builder) buildField(field *ast.Field) *proto.Field {
	f := &proto.Field{
		Id:      newUUID(),
		Prefix:  b.prefix(field.Pos()),
		Markers: &proto.Markers{Id: newUUID()},
	}

	for i, name := range field.Names {
		if i > 0 {
			b.consumeText(",")
		}
		b.prefix(name.Pos())
		b.consume(name.Pos(), name.Name)
		f.Names = append(f.Names, name.Name)
	}

	if field.Type != nil {
		f.Type = b.buildExpr(field.Type)
	}

	if field.Tag != nil {
		f.Tag = field.Tag.Value
	}

	return f
}

func (b *Builder) buildGoType(expr ast.Expr) *proto.GoType {
	switch t := expr.(type) {
	case *ast.Ident:
		return &proto.GoType{
			Type: &proto.GoType_Named{
				Named: &proto.NamedType{
					Name: t.Name,
				},
			},
		}
	case *ast.StarExpr:
		return &proto.GoType{
			Type: &proto.GoType_Pointer{
				Pointer: &proto.PointerType{
					Elem: b.buildGoType(t.X),
				},
			},
		}
	case *ast.ArrayType:
		if t.Len == nil {
			return &proto.GoType{
				Type: &proto.GoType_Slice{
					Slice: &proto.SliceType{
						Elem: b.buildGoType(t.Elt),
					},
				},
			}
		}
		return &proto.GoType{
			Type: &proto.GoType_Array{
				Array: &proto.ArrayType{
					Elem: b.buildGoType(t.Elt),
				},
			},
		}
	case *ast.MapType:
		return &proto.GoType{
			Type: &proto.GoType_Map{
				Map: &proto.MapType{
					Key:   b.buildGoType(t.Key),
					Value: b.buildGoType(t.Value),
				},
			},
		}
	}
	return nil
}

func (b *Builder) convertType(t types.Type) *proto.GoType {
	if t == nil {
		return nil
	}

	switch typ := t.(type) {
	case *types.Basic:
		return &proto.GoType{
			Type: &proto.GoType_Basic{
				Basic: &proto.BasicType{
					Kind: typ.Name(),
				},
			},
		}
	case *types.Named:
		obj := typ.Obj()
		return &proto.GoType{
			Type: &proto.GoType_Named{
				Named: &proto.NamedType{
					PackagePath: obj.Pkg().Path(),
					Name:        obj.Name(),
				},
			},
		}
	case *types.Pointer:
		return &proto.GoType{
			Type: &proto.GoType_Pointer{
				Pointer: &proto.PointerType{
					Elem: b.convertType(typ.Elem()),
				},
			},
		}
	case *types.Slice:
		return &proto.GoType{
			Type: &proto.GoType_Slice{
				Slice: &proto.SliceType{
					Elem: b.convertType(typ.Elem()),
				},
			},
		}
	case *types.Map:
		return &proto.GoType{
			Type: &proto.GoType_Map{
				Map: &proto.MapType{
					Key:   b.convertType(typ.Key()),
					Value: b.convertType(typ.Elem()),
				},
			},
		}
	}
	return nil
}

func (b *Builder) offset(pos token.Pos) int {
	if !pos.IsValid() {
		return b.cursor
	}
	off := b.fset.Position(pos).Offset
	if off < 0 {
		return 0
	}
	if off > len(b.src) {
		return len(b.src)
	}
	return off
}

// prefix claims the source between the cursor and pos as the next node's prefix.
func (b *Builder) prefix(pos token.Pos) *proto.Space {
	off := b.offset(pos)
	if off <= b.cursor {
		return &proto.Space{}
	}
	ws := string(b.src[b.cursor:off])
	b.cursor = off
	return &proto.Space{Whitespace: ws}
}

// consume advances the cursor past a token the printer emits literally, so the token
// does not leak into the next node's prefix and get printed a second time.
func (b *Builder) consume(pos token.Pos, text string) {
	off := b.offset(pos)
	if off < b.cursor {
		return
	}
	end := off + len(text)
	if end > len(b.src) {
		end = len(b.src)
	}
	b.cursor = end
}

// consumeText skips whitespace at the cursor and advances past text if that is what
// comes next. Used for tokens whose position the AST does not record.
func (b *Builder) consumeText(text string) {
	i := b.cursor
	for i < len(b.src) && isSpace(b.src[i]) {
		i++
	}
	if i+len(text) <= len(b.src) && string(b.src[i:i+len(text)]) == text {
		b.cursor = i + len(text)
	}
}

// rest returns everything left in the source, for the trailing Space on a file.
func (b *Builder) rest() *proto.Space {
	if b.cursor >= len(b.src) {
		return &proto.Space{}
	}
	ws := string(b.src[b.cursor:])
	b.cursor = len(b.src)
	return &proto.Space{Whitespace: ws}
}

func isSpace(c byte) bool {
	return c == ' ' || c == '\t' || c == '\n' || c == '\r'
}

func newUUID() *proto.UUID {
	id := uuid.New()
	return &proto.UUID{
		MostSigBits: int64(id[0])<<56 | int64(id[1])<<48 | int64(id[2])<<40 | int64(id[3])<<32 |
			int64(id[4])<<24 | int64(id[5])<<16 | int64(id[6])<<8 | int64(id[7]),
		LeastSigBits: int64(id[8])<<56 | int64(id[9])<<48 | int64(id[10])<<40 | int64(id[11])<<32 |
			int64(id[12])<<24 | int64(id[13])<<16 | int64(id[14])<<8 | int64(id[15]),
	}
}
