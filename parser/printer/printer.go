package printer

import (
	"fmt"
	"strings"

	"github.com/openrewrite/rewrite-go/parser/proto"
)

type Printer struct {
	output strings.Builder
}

func New() *Printer {
	return &Printer{}
}

func (p *Printer) Print(file *proto.GoFile) string {
	p.output.Reset()
	p.printFile(file)
	return p.output.String()
}

func (p *Printer) printFile(file *proto.GoFile) {
	p.printSpace(file.Prefix)

	if file.PackageClause != nil {
		p.printPackageClause(file.PackageClause)
	}

	for _, imp := range file.Imports {
		p.printImportDecl(imp)
	}

	for _, decl := range file.Declarations {
		p.printDecl(decl)
	}

	p.printSpace(file.Eof)
}

func (p *Printer) printPackageClause(pc *proto.PackageClause) {
	p.printSpace(pc.Prefix)
	p.write("package")
	p.printIdent(pc.Name)
}

func (p *Printer) printImportDecl(id *proto.ImportDecl) {
	p.printSpace(id.Prefix)
	p.write("import")

	if id.Grouped {
		p.write(" (")
		for _, spec := range id.Specs {
			p.printImportSpec(spec)
		}
		p.printSpace(id.End)
		p.write(")")
	} else if len(id.Specs) > 0 {
		p.printImportSpec(id.Specs[0])
	}
}

func (p *Printer) printImportSpec(spec *proto.ImportSpec) {
	p.printSpace(spec.Prefix)
	if spec.Alias != nil {
		p.printIdent(spec.Alias)
	}
	p.printBasicLit(spec.Path)
}

func (p *Printer) printDecl(decl *proto.Decl) {
	switch d := decl.Decl.(type) {
	case *proto.Decl_FuncDecl:
		p.printFuncDecl(d.FuncDecl)
	case *proto.Decl_GenDecl:
		p.printGenDecl(d.GenDecl)
	}
}

func (p *Printer) printFuncDecl(fd *proto.FuncDecl) {
	p.printSpace(fd.Prefix)
	p.write("func")

	if fd.Recv != nil {
		p.write(" (")
		p.printField(fd.Recv)
		p.write(")")
	}

	p.printIdent(fd.Name)

	if fd.Type != nil {
		p.printFuncType(fd.Type)
	}

	if fd.Body != nil {
		p.printBlockStmt(fd.Body)
	}
}

func (p *Printer) printGenDecl(gd *proto.GenDecl) {
	p.printSpace(gd.Prefix)
	p.write(gd.Tok)

	if gd.Grouped {
		p.write(" (")
		for _, spec := range gd.Specs {
			p.printSpec(spec)
		}
		p.printSpace(gd.End)
		p.write(")")
	} else if len(gd.Specs) > 0 {
		p.printSpec(gd.Specs[0])
	}
}

func (p *Printer) printSpec(spec *proto.Spec) {
	switch s := spec.Spec.(type) {
	case *proto.Spec_ValueSpec:
		p.printValueSpec(s.ValueSpec)
	case *proto.Spec_TypeSpec:
		p.printTypeSpec(s.TypeSpec)
	}
}

func (p *Printer) printValueSpec(vs *proto.ValueSpec) {
	p.printSpace(vs.Prefix)
	for i, name := range vs.Names {
		if i > 0 {
			p.write(",")
		}
		p.printIdent(name)
	}

	if vs.Type != nil {
		p.printExpr(vs.Type)
	}

	if len(vs.Values) > 0 {
		p.write(" =")
		for i, val := range vs.Values {
			if i > 0 {
				p.write(",")
			}
			p.printExpr(val)
		}
	}
}

func (p *Printer) printTypeSpec(ts *proto.TypeSpec) {
	p.printSpace(ts.Prefix)
	p.printIdent(ts.Name)

	if ts.Assign {
		p.write(" =")
	}

	p.printExpr(ts.Type)
}

func (p *Printer) printStmt(stmt *proto.Stmt) {
	switch s := stmt.Stmt.(type) {
	case *proto.Stmt_BlockStmt:
		p.printBlockStmt(s.BlockStmt)
	case *proto.Stmt_ExprStmt:
		p.printSpace(s.ExprStmt.Prefix)
		p.printExpr(s.ExprStmt.Expr)
	case *proto.Stmt_AssignStmt:
		p.printAssignStmt(s.AssignStmt)
	case *proto.Stmt_ReturnStmt:
		p.printReturnStmt(s.ReturnStmt)
	case *proto.Stmt_IfStmt:
		p.printIfStmt(s.IfStmt)
	case *proto.Stmt_ForStmt:
		p.printForStmt(s.ForStmt)
	case *proto.Stmt_RangeStmt:
		p.printRangeStmt(s.RangeStmt)
	case *proto.Stmt_DeclStmt:
		p.printSpace(s.DeclStmt.Prefix)
		p.printDecl(s.DeclStmt.Decl)
	}
}

func (p *Printer) printBlockStmt(bs *proto.BlockStmt) {
	p.printSpace(bs.Prefix)
	p.write("{")
	for _, stmt := range bs.Stmts {
		p.printStmt(stmt)
	}
	p.printSpace(bs.End)
	p.write("}")
}

func (p *Printer) printAssignStmt(as *proto.AssignStmt) {
	p.printSpace(as.Prefix)
	for i, lhs := range as.Lhs {
		if i > 0 {
			p.write(",")
		}
		p.printExpr(lhs)
	}
	p.write(" ")
	p.write(as.Tok)
	for i, rhs := range as.Rhs {
		if i > 0 {
			p.write(",")
		}
		p.printExpr(rhs)
	}
}

func (p *Printer) printReturnStmt(rs *proto.ReturnStmt) {
	p.printSpace(rs.Prefix)
	p.write("return")
	if len(rs.Results) > 0 {
		for i, result := range rs.Results {
			if i > 0 {
				p.write(",")
			}
			p.printExpr(result)
		}
	}
}

func (p *Printer) printIfStmt(is *proto.IfStmt) {
	p.printSpace(is.Prefix)
	p.write("if")

	if is.Init != nil {
		p.printStmt(is.Init)
		p.write(";")
	}

	p.printExpr(is.Cond)
	p.printBlockStmt(is.Body)

	if is.ElseStmt != nil {
		p.write(" else")
		p.printStmt(is.ElseStmt)
	}
}

func (p *Printer) printForStmt(fs *proto.ForStmt) {
	p.printSpace(fs.Prefix)
	p.write("for ")

	if fs.Init != nil {
		p.printStmt(fs.Init)
		p.write("; ")
		if fs.Cond != nil {
			p.printExpr(fs.Cond)
		}
		p.write("; ")
		if fs.Post != nil {
			p.printStmt(fs.Post)
		}
	} else if fs.Cond != nil {
		p.printExpr(fs.Cond)
	}

	p.write(" ")
	p.printBlockStmt(fs.Body)
}

func (p *Printer) printRangeStmt(rs *proto.RangeStmt) {
	p.printSpace(rs.Prefix)
	p.write("for ")

	if rs.Key != nil {
		p.printExpr(rs.Key)
		if rs.Value != nil {
			p.write(",")
			p.printExpr(rs.Value)
		}
		p.write(" ")
		p.write(rs.Tok)
		p.write(" ")
	}

	p.write("range ")
	p.printExpr(rs.X)
	p.write(" ")
	p.printBlockStmt(rs.Body)
}

func (p *Printer) printExpr(expr *proto.Expr) {
	switch e := expr.Expr.(type) {
	case *proto.Expr_Ident:
		p.printIdent(e.Ident)
	case *proto.Expr_BasicLit:
		p.printBasicLit(e.BasicLit)
	case *proto.Expr_CallExpr:
		p.printCallExpr(e.CallExpr)
	case *proto.Expr_SelectorExpr:
		p.printSelectorExpr(e.SelectorExpr)
	case *proto.Expr_BinaryExpr:
		p.printBinaryExpr(e.BinaryExpr)
	case *proto.Expr_UnaryExpr:
		p.printUnaryExpr(e.UnaryExpr)
	case *proto.Expr_ParenExpr:
		p.printParenExpr(e.ParenExpr)
	case *proto.Expr_CompositeLit:
		p.printCompositeLit(e.CompositeLit)
	case *proto.Expr_IndexExpr:
		p.printIndexExpr(e.IndexExpr)
	case *proto.Expr_StarExpr:
		p.printStarExpr(e.StarExpr)
	case *proto.Expr_KeyValueExpr:
		p.printKeyValueExpr(e.KeyValueExpr)
	case *proto.Expr_StructTypeExpr:
		p.printStructTypeExpr(e.StructTypeExpr)
	case *proto.Expr_InterfaceTypeExpr:
		p.printInterfaceTypeExpr(e.InterfaceTypeExpr)
	case *proto.Expr_ArrayTypeExpr:
		p.printArrayTypeExpr(e.ArrayTypeExpr)
	case *proto.Expr_SliceTypeExpr:
		p.printSliceTypeExpr(e.SliceTypeExpr)
	case *proto.Expr_MapTypeExpr:
		p.printMapTypeExpr(e.MapTypeExpr)
	case *proto.Expr_ChanTypeExpr:
		p.printChanTypeExpr(e.ChanTypeExpr)
	case *proto.Expr_PointerTypeExpr:
		p.printPointerTypeExpr(e.PointerTypeExpr)
	case *proto.Expr_FuncTypeExpr:
		p.printFuncTypeExpr(e.FuncTypeExpr)
	case *proto.Expr_TypeAssertExpr:
		p.printTypeAssertExpr(e.TypeAssertExpr)
	}
}

func (p *Printer) printMethod(m *proto.Method) {
	p.printSpace(m.Prefix)
	p.write(m.Name)
	if m.Type != nil {
		p.printFuncType(m.Type)
	}
}

func (p *Printer) printStructTypeExpr(st *proto.StructTypeExpr) {
	p.printSpace(st.Prefix)
	p.write("struct {")
	for _, field := range st.Fields {
		p.printField(field)
	}
	p.write("}")
}

func (p *Printer) printInterfaceTypeExpr(it *proto.InterfaceTypeExpr) {
	p.printSpace(it.Prefix)
	p.write("interface {")
	for _, method := range it.Methods {
		p.printMethod(method)
	}
	p.write("}")
}

func (p *Printer) printArrayTypeExpr(at *proto.ArrayTypeExpr) {
	p.printSpace(at.Prefix)
	p.write("[")
	if at.Len != nil {
		p.printExpr(at.Len)
	}
	p.write("]")
	if at.Elt != nil {
		p.printExpr(at.Elt)
	}
}

func (p *Printer) printSliceTypeExpr(st *proto.SliceTypeExpr) {
	p.printSpace(st.Prefix)
	p.write("[]")
	if st.Elt != nil {
		p.printExpr(st.Elt)
	}
}

func (p *Printer) printMapTypeExpr(mt *proto.MapTypeExpr) {
	p.printSpace(mt.Prefix)
	p.write("map[")
	if mt.Key != nil {
		p.printExpr(mt.Key)
	}
	p.write("]")
	if mt.Value != nil {
		p.printExpr(mt.Value)
	}
}

func (p *Printer) printChanTypeExpr(ct *proto.ChanTypeExpr) {
	p.printSpace(ct.Prefix)
	switch ct.Dir {
	case 1:
		p.write("chan<-")
	case 2:
		p.write("<-chan")
	default:
		p.write("chan")
	}
	if ct.Value != nil {
		p.printExpr(ct.Value)
	}
}

func (p *Printer) printPointerTypeExpr(pt *proto.PointerTypeExpr) {
	p.printSpace(pt.Prefix)
	p.write("*")
	if pt.Base != nil {
		p.printExpr(pt.Base)
	}
}

func (p *Printer) printFuncTypeExpr(ft *proto.FuncTypeExpr) {
	p.printSpace(ft.Prefix)
	p.write("func(")
	for i, param := range ft.Params {
		if i > 0 {
			p.write(",")
		}
		p.printField(param)
	}
	p.write(")")
	for i, result := range ft.Results {
		if i > 0 {
			p.write(",")
		}
		p.printField(result)
	}
}

func (p *Printer) printTypeAssertExpr(ta *proto.TypeAssertExpr) {
	p.printSpace(ta.Prefix)
	if ta.X != nil {
		p.printExpr(ta.X)
	}
	p.write(".(")
	if ta.Type != nil {
		p.printExpr(ta.Type)
	}
	p.write(")")
}

func (p *Printer) printIdent(id *proto.Ident) {
	p.printSpace(id.Prefix)
	p.write(id.Name)
}

func (p *Printer) printBasicLit(lit *proto.BasicLit) {
	p.printSpace(lit.Prefix)
	p.write(lit.Value)
}

func (p *Printer) printCallExpr(ce *proto.CallExpr) {
	p.printSpace(ce.Prefix)
	p.printExpr(ce.Fun)
	p.write("(")
	for i, arg := range ce.Args {
		if i > 0 {
			p.write(",")
		}
		p.printExpr(arg)
	}
	if ce.Ellipsis {
		p.write("...")
	}
	p.write(")")
}

func (p *Printer) printSelectorExpr(se *proto.SelectorExpr) {
	p.printSpace(se.Prefix)
	p.printExpr(se.X)
	p.write(".")
	p.printIdent(se.Sel)
}

func (p *Printer) printBinaryExpr(be *proto.BinaryExpr) {
	p.printSpace(be.Prefix)
	p.printExpr(be.X)
	p.write(" ")
	p.write(be.Op)
	p.printExpr(be.Y)
}

func (p *Printer) printUnaryExpr(ue *proto.UnaryExpr) {
	p.printSpace(ue.Prefix)
	p.write(ue.Op)
	p.printExpr(ue.X)
}

func (p *Printer) printParenExpr(pe *proto.ParenExpr) {
	p.printSpace(pe.Prefix)
	p.write("(")
	p.printExpr(pe.X)
	p.write(")")
}

func (p *Printer) printCompositeLit(cl *proto.CompositeLit) {
	p.printSpace(cl.Prefix)
	if cl.Type != nil {
		p.printExpr(cl.Type)
	}
	p.write("{")
	for i, elt := range cl.Elts {
		if i > 0 {
			p.write(",")
		}
		p.printExpr(elt)
	}
	p.write("}")
}

func (p *Printer) printIndexExpr(ie *proto.IndexExpr) {
	p.printSpace(ie.Prefix)
	p.printExpr(ie.X)
	p.write("[")
	p.printExpr(ie.Index)
	p.write("]")
}

func (p *Printer) printStarExpr(se *proto.StarExpr) {
	p.printSpace(se.Prefix)
	p.write("*")
	p.printExpr(se.X)
}

func (p *Printer) printKeyValueExpr(kve *proto.KeyValueExpr) {
	p.printSpace(kve.Prefix)
	p.printExpr(kve.Key)
	p.write(": ")
	p.printExpr(kve.Value)
}

func (p *Printer) printField(field *proto.Field) {
	p.printSpace(field.Prefix)
	for i, name := range field.Names {
		if i > 0 {
			p.write(",")
		}
		p.write(name)
	}
	if field.Type != nil {
		p.printExpr(field.Type)
	}
	if field.Tag != "" {
		p.write(field.Tag)
	}
}

func (p *Printer) printFuncType(ft *proto.FuncType) {
	p.write("(")
	for i, param := range ft.Params {
		if i > 0 {
			p.write(",")
		}
		p.printField(param)
	}
	p.write(")")

	if len(ft.Results) > 0 {
		if len(ft.Results) == 1 && len(ft.Results[0].Names) == 0 {
			p.printField(ft.Results[0])
		} else {
			p.write("(")
			for i, result := range ft.Results {
				if i > 0 {
					p.write(",")
				}
				p.printField(result)
			}
			p.write(")")
		}
	}
}

func (p *Printer) printGoType(t *proto.GoType) {
	switch typ := t.Type.(type) {
	case *proto.GoType_Basic:
		p.write(typ.Basic.Kind)
	case *proto.GoType_Named:
		if typ.Named.PackagePath != "" {
			parts := strings.Split(typ.Named.PackagePath, "/")
			p.write(parts[len(parts)-1])
			p.write(".")
		}
		p.write(typ.Named.Name)
	case *proto.GoType_Pointer:
		p.write("*")
		p.printGoType(typ.Pointer.Elem)
	case *proto.GoType_Slice:
		p.write("[]")
		p.printGoType(typ.Slice.Elem)
	case *proto.GoType_Array:
		p.write("[")
		if typ.Array.Len > 0 {
			p.write(fmt.Sprintf("%d", typ.Array.Len))
		}
		p.write("]")
		p.printGoType(typ.Array.Elem)
	case *proto.GoType_Map:
		p.write("map[")
		p.printGoType(typ.Map.Key)
		p.write("]")
		p.printGoType(typ.Map.Value)
	}
}

func (p *Printer) printSpace(space *proto.Space) {
	if space == nil {
		return
	}
	p.write(space.Whitespace)
}

func (p *Printer) write(s string) {
	p.output.WriteString(s)
}
