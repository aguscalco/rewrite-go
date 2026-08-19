package org.openrewrite.go.tree;

import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;

public class GoVisitor<P> extends TreeVisitor<Tree, P> {
    
    public boolean isAcceptable(Tree sourceFile, P p) {
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
        return packageClause;
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
        if (f.getType() != null) {
            f = f.withType((FuncType) visit(f.getType(), p));
        }
        if (f.getBody() != null) {
            f = f.withBody((BlockStmt) visit(f.getBody(), p));
        }
        return f;
    }
    
    public Tree visitGenDecl(GenDecl genDecl, P p) {
        return visitGo(genDecl, p);
    }
    
    public Tree visitValueSpec(ValueSpec valueSpec, P p) {
        ValueSpec v = valueSpec;
        if (v.getType() != null) {
            v = v.withType((Expr) visit(v.getType(), p));
        }
        v = v.withValues(ListUtils.map(v.getValues(), val -> (Expr) visit(val, p)));
        return v;
    }
    
    public Tree visitTypeSpec(TypeSpec typeSpec, P p) {
        TypeSpec t = typeSpec;
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
        return visitGo(assignStmt, p);
    }
    
    public Tree visitReturnStmt(ReturnStmt returnStmt, P p) {
        return visitGo(returnStmt, p);
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
        return visitGo(binaryExpr, p);
    }
    
    public Tree visitUnaryExpr(UnaryExpr unaryExpr, P p) {
        return visitGo(unaryExpr, p);
    }
    
    public Tree visitInterfaceTypeExpr(InterfaceTypeExpr interfaceTypeExpr, P p) {
        return visitGo(interfaceTypeExpr, p);
    }
    
    public Tree visitFuncTypeExpr(FuncTypeExpr funcTypeExpr, P p) {
        return visitGo(funcTypeExpr, p);
    }
    
    public Tree visitArrayTypeExpr(ArrayTypeExpr arrayTypeExpr, P p) {
        return visitGo(arrayTypeExpr, p);
    }
    
    public Tree visitSliceTypeExpr(SliceTypeExpr sliceTypeExpr, P p) {
        return visitGo(sliceTypeExpr, p);
    }
    
    public Tree visitMapTypeExpr(MapTypeExpr mapTypeExpr, P p) {
        return visitGo(mapTypeExpr, p);
    }
    
    public Tree visitChanTypeExpr(ChanTypeExpr chanTypeExpr, P p) {
        return visitGo(chanTypeExpr, p);
    }
    
    public Tree visitStructTypeExpr(StructTypeExpr structTypeExpr, P p) {
        return visitGo(structTypeExpr, p);
    }
    
    public Tree visitPointerTypeExpr(PointerTypeExpr pointerTypeExpr, P p) {
        return visitGo(pointerTypeExpr, p);
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
        return visitGo(method, p);
    }
}
