package org.openrewrite.go.tree;

import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;

public class GoVisitor<P> extends TreeVisitor<Tree, P> {
    
    @Override
    public boolean isAcceptable(SourceFile sourceFile, P p) {
        return sourceFile instanceof GoFile;
    }
    
    @Override
    public String getLanguage() {
        return "go";
    }
    
    public Tree visitGo(Go go, P p) {
        return go;
    }
    
    public Tree visitGoFile(GoFile goFile, P p) {
        GoFile g = goFile;
        if (g.getPackageClause() != null) {
            g = g.withPackageClause((PackageClause) visit(g.getPackageClause(), p));
        }
        g = g.withImports(ListUtils.map(g.getImports(), imp -> (ImportDecl) visit(imp, p)));
        g = g.withDeclarations(ListUtils.map(g.getDeclarations(), decl -> (Decl) visit(decl, p)));
        return g;
    }
    
    public Tree visitPackageClause(PackageClause packageClause, P p) {
        PackageClause pc = packageClause;
        if (pc.getName() != null) {
            pc = pc.withName((Ident) visit(pc.getName(), p));
        }
        return pc;
    }
    
    public Tree visitImportDecl(ImportDecl importDecl, P p) {
        ImportDecl i = importDecl;
        i = i.withSpecs(ListUtils.map(i.getSpecs(), spec -> (ImportSpec) visit(spec, p)));
        return i;
    }
    
    public Tree visitImportSpec(ImportSpec importSpec, P p) {
        ImportSpec i = importSpec;
        if (i.getAlias() != null) {
            i = i.withAlias((Ident) visit(i.getAlias(), p));
        }
        i = i.withPath((BasicLit) visit(i.getPath(), p));
        return i;
    }
    
    public Tree visitIdent(Ident ident, P p) {
        return visitGo(ident, p);
    }
    
    public Tree visitBasicLit(BasicLit basicLit, P p) {
        return visitGo(basicLit, p);
    }
    
    public Tree visitFuncDecl(FuncDecl funcDecl, P p) {
        FuncDecl f = funcDecl;
        if (f.getRecv() != null) {
            f = f.withRecv((Field) visit(f.getRecv(), p));
        }
        f = f.withName((Ident) visit(f.getName(), p));
        f = f.withTypeParams(ListUtils.map(f.getTypeParams(), tp -> (TypeParamDecl) visit(tp, p)));
        if (f.getType() != null) {
            f = f.withType((FuncType) visit(f.getType(), p));
        }
        if (f.getBody() != null) {
            f = f.withBody((BlockStmt) visit(f.getBody(), p));
        }
        return f;
    }
    
    public Tree visitGenDecl(GenDecl genDecl, P p) {
        GenDecl g = genDecl;
        g = g.withSpecs(ListUtils.map(g.getSpecs(), spec -> (Spec) visit(spec, p)));
        return g;
    }
    
    public Tree visitValueSpec(ValueSpec valueSpec, P p) {
        ValueSpec v = valueSpec;
        v = v.withNames(ListUtils.map(v.getNames(), n -> (Ident) visit(n, p)));
        if (v.getType() != null) {
            v = v.withType((Expr) visit(v.getType(), p));
        }
        v = v.withValues(ListUtils.map(v.getValues(), val -> (Expr) visit(val, p)));
        return v;
    }
    
    public Tree visitTypeSpec(TypeSpec typeSpec, P p) {
        TypeSpec t = typeSpec;
        if (t.getName() != null) {
            t = t.withName((Ident) visit(t.getName(), p));
        }
        t = t.withTypeParams(ListUtils.map(t.getTypeParams(), tp -> (TypeParamDecl) visit(tp, p)));
        if (t.getType() != null) {
            t = t.withType((Expr) visit(t.getType(), p));
        }
        return t;
    }
    
    public Tree visitField(Field field, P p) {
        Field f = field;
        if (f.getType() != null) {
            f = f.withType((Expr) visit(f.getType(), p));
        }
        return f;
    }
    
    public Tree visitBlockStmt(BlockStmt blockStmt, P p) {
        BlockStmt b = blockStmt;
        b = b.withStmts(ListUtils.map(b.getStmts(), stmt -> (Stmt) visit(stmt, p)));
        return b;
    }
    
    public Tree visitExprStmt(ExprStmt exprStmt, P p) {
        ExprStmt e = exprStmt;
        e = e.withExpr((Expr) visit(e.getExpr(), p));
        return e;
    }
    
    public Tree visitAssignStmt(AssignStmt assignStmt, P p) {
        AssignStmt a = assignStmt;
        a = a.withLhs(ListUtils.map(a.getLhs(), e -> (Expr) visit(e, p)));
        a = a.withRhs(ListUtils.map(a.getRhs(), e -> (Expr) visit(e, p)));
        return a;
    }
    
    public Tree visitReturnStmt(ReturnStmt returnStmt, P p) {
        ReturnStmt r = returnStmt;
        r = r.withResults(ListUtils.map(r.getResults(), e -> (Expr) visit(e, p)));
        return r;
    }
    
    public Tree visitIfStmt(IfStmt ifStmt, P p) {
        IfStmt i = ifStmt;
        if (i.getInit() != null) {
            i = i.withInit((Stmt) visit(i.getInit(), p));
        }
        if (i.getCond() != null) {
            i = i.withCond((Expr) visit(i.getCond(), p));
        }
        if (i.getBody() != null) {
            i = i.withBody((BlockStmt) visit(i.getBody(), p));
        }
        if (i.getElseStmt() != null) {
            i = i.withElseStmt((Stmt) visit(i.getElseStmt(), p));
        }
        return i;
    }
    
    public Tree visitCallExpr(CallExpr callExpr, P p) {
        CallExpr c = callExpr;
        c = c.withFun((Expr) visit(c.getFun(), p));
        c = c.withArgs(ListUtils.map(c.getArgs(), arg -> (Expr) visit(arg, p)));
        return c;
    }
    
    public Tree visitSelectorExpr(SelectorExpr selectorExpr, P p) {
        SelectorExpr s = selectorExpr;
        s = s.withX((Expr) visit(s.getX(), p));
        s = s.withSel((Ident) visit(s.getSel(), p));
        return s;
    }
    
    public Tree visitBinaryExpr(BinaryExpr binaryExpr, P p) {
        BinaryExpr b = binaryExpr;
        if (b.getX() != null) {
            b = b.withX((Expr) visit(b.getX(), p));
        }
        if (b.getY() != null) {
            b = b.withY((Expr) visit(b.getY(), p));
        }
        return b;
    }
    
    public Tree visitUnaryExpr(UnaryExpr unaryExpr, P p) {
        UnaryExpr u = unaryExpr;
        if (u.getX() != null) {
            u = u.withX((Expr) visit(u.getX(), p));
        }
        return u;
    }
    
    public Tree visitInterfaceTypeExpr(InterfaceTypeExpr interfaceTypeExpr, P p) {
        InterfaceTypeExpr i = interfaceTypeExpr;
        i = i.withMethods(ListUtils.map(i.getMethods(), m -> (Method) visit(m, p)));
        return i;
    }
    
    public Tree visitFuncTypeExpr(FuncTypeExpr funcTypeExpr, P p) {
        FuncTypeExpr f = funcTypeExpr;
        f = f.withParams(ListUtils.map(f.getParams(), param -> (Field) visit(param, p)));
        f = f.withResults(ListUtils.map(f.getResults(), result -> (Field) visit(result, p)));
        return f;
    }
    
    public Tree visitArrayTypeExpr(ArrayTypeExpr arrayTypeExpr, P p) {
        ArrayTypeExpr a = arrayTypeExpr;
        if (a.getLen() != null) {
            a = a.withLen((Expr) visit(a.getLen(), p));
        }
        if (a.getElt() != null) {
            a = a.withElt((Expr) visit(a.getElt(), p));
        }
        return a;
    }
    
    public Tree visitSliceTypeExpr(SliceTypeExpr sliceTypeExpr, P p) {
        SliceTypeExpr s = sliceTypeExpr;
        if (s.getElt() != null) {
            s = s.withElt((Expr) visit(s.getElt(), p));
        }
        return s;
    }
    
    public Tree visitMapTypeExpr(MapTypeExpr mapTypeExpr, P p) {
        MapTypeExpr m = mapTypeExpr;
        if (m.getKey() != null) {
            m = m.withKey((Expr) visit(m.getKey(), p));
        }
        if (m.getValue() != null) {
            m = m.withValue((Expr) visit(m.getValue(), p));
        }
        return m;
    }
    
    public Tree visitChanTypeExpr(ChanTypeExpr chanTypeExpr, P p) {
        ChanTypeExpr c = chanTypeExpr;
        if (c.getValue() != null) {
            c = c.withValue((Expr) visit(c.getValue(), p));
        }
        return c;
    }
    
    public Tree visitStructTypeExpr(StructTypeExpr structTypeExpr, P p) {
        StructTypeExpr s = structTypeExpr;
        s = s.withFields(ListUtils.map(s.getFields(), field -> (Field) visit(field, p)));
        return s;
    }
    
    public Tree visitPointerTypeExpr(PointerTypeExpr pointerTypeExpr, P p) {
        PointerTypeExpr pt = pointerTypeExpr;
        if (pt.getBase() != null) {
            pt = pt.withBase((Expr) visit(pt.getBase(), p));
        }
        return pt;
    }
    
    public Tree visitTypeAssertExpr(TypeAssertExpr typeAssertExpr, P p) {
        TypeAssertExpr t = typeAssertExpr;
        if (t.getX() != null) {
            t = t.withX((Expr) visit(t.getX(), p));
        }
        if (t.getType() != null) {
            t = t.withType((Expr) visit(t.getType(), p));
        }
        return t;
    }
    
    public Tree visitStarExpr(StarExpr starExpr, P p) {
        StarExpr s = starExpr;
        if (s.getX() != null) {
            s = s.withX((Expr) visit(s.getX(), p));
        }
        return s;
    }
    
    public Tree visitFuncType(FuncType funcType, P p) {
        FuncType f = funcType;
        f = f.withParams(ListUtils.map(f.getParams(), param -> (Field) visit(param, p)));
        f = f.withResults(ListUtils.map(f.getResults(), result -> (Field) visit(result, p)));
        return f;
    }
    
    public Tree visitTypeParamDecl(TypeParamDecl typeParamDecl, P p) {
        return visitGo(typeParamDecl, p);
    }
    
    public Tree visitGoType(GoType goType, P p) {
        return visitGo(goType, p);
    }
    
    public Tree visitMethod(Method method, P p) {
        Method m = method;
        if (m.getType() != null) {
            m = m.withType((FuncType) visit(m.getType(), p));
        }
        return m;
    }
}
