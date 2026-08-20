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
	p.write("package ")
	p.printIdent(pc.Name)
}

func (p *Printer) printImportDecl(id *proto.ImportDecl) {
	p.printSpace(id.Prefix)
	p.write("import ")

	if id.Grouped {
		p.write("(")
		for _, spec := range id.Specs {
			p.printImportSpec(spec)
		}
		p.write(")")
	} else if len(id.Specs) > 0 {
		p.printImportSpec(id.Specs[0])
	}

	p.printSpace(id.End)
}

func (p *Printer) printImportSpec(spec *proto.ImportSpec) {
	p.printSpace(spec.Prefix)
	if spec.Alias != nil {
		p.printIdent(spec.Alias)
		p.write(" ")
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
	p.write("func ")

	if fd.Recv != nil {
		p.write("(")
		p.printField(fd.Recv)
		p.write(") ")
	}

	p.printIdent(fd.Name)

	if fd.Type != nil {
		p.printFuncType(fd.Type)
	}

	if fd.Body != nil {
		p.write(" ")
		p.printBlockStmt(fd.Body)
	}
}

func (p *Printer) printGenDecl(gd *proto.GenDecl) {
	p.printSpace(gd.Prefix)
	p.write(gd.Tok)
	p.write(" ")

	if gd.Grouped {
		p.write("(")
		for _, spec := range gd.Specs {
			p.printSpec(spec)
		}
		p.write(")")
	} else if len(gd.Specs) > 0 {
		p.printSpec(gd.Specs[0])
	}

	p.printSpace(gd.End)
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
			p.write(", ")
		}
		p.printIdent(name)
	}

	if vs.Type != nil {
		p.write(" ")
		p.printExpr(vs.Type)
	}

	if len(vs.Values) > 0 {
		p.write(" = ")
		for i, val := range vs.Values {
			if i > 0 {
				p.write(", ")
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

	p.write(" ")
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
	p.write("}")
	p.printSpace(bs.End)
}

func (p *Printer) printAssignStmt(as *proto.AssignStmt) {
	p.printSpace(as.Prefix)
	for i, lhs := range as.Lhs {
		if i > 0 {
			p.write(", ")
		}
		p.printExpr(lhs)
	}
	p.write(" ")
	p.write(as.Tok)
	p.write(" ")
	for i, rhs := range as.Rhs {
		if i > 0 {
			p.write(", ")
		}
		p.printExpr(rhs)
	}
}

func (p *Printer) printReturnStmt(rs *proto.ReturnStmt) {
	p.printSpace(rs.Prefix)
	p.write("return")
	if len(rs.Results) > 0 {
		p.write(" ")
		for i, result := range rs.Results {
			if i > 0 {
				p.write(", ")
			}
			p.printExpr(result)
		}
	}
}

func (p *Printer) printIfStmt(is *proto.IfStmt) {
	p.printSpace(is.Prefix)
	p.write("if ")

	if is.Init != nil {
		p.printStmt(is.Init)
		p.write("; ")
	}

	p.printExpr(is.Cond)
	p.write(" ")
	p.printBlockStmt(is.Body)

	if is.ElseStmt != nil {
		p.write(" else ")
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
			p.write(", ")
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
	}
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
			p.write(", ")
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
	p.write(" ")
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
			p.write(", ")
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
			p.write(", ")
		}
		p.write(name)
	}
	if field.Type != nil {
		p.write(" ")
		p.printExpr(field.Type)
	}
	if field.Tag != "" {
		p.write(" ")
		p.write(field.Tag)
	}
}

func (p *Printer) printFuncType(ft *proto.FuncType) {
	p.write("(")
	for i, param := range ft.Params {
		if i > 0 {
			p.write(", ")
		}
		p.printField(param)
	}
	p.write(")")

	if len(ft.Results) > 0 {
		p.write(" ")
		if len(ft.Results) == 1 && len(ft.Results[0].Names) == 0 {
			p.printExpr(ft.Results[0].Type)
		} else {
			p.write("(")
			for i, result := range ft.Results {
				if i > 0 {
					p.write(", ")
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
