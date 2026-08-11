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
        return visitGo(valueSpec, p);
    }
    
    public Tree visitTypeSpec(TypeSpec typeSpec, P p) {
        return visitGo(typeSpec, p);
    }
    
    public Tree visitField(Field field, P p) {
        return visitGo(field, p);
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
    
    public Tree visitFuncType(FuncType funcType, P p) {
        return visitGo(funcType, p);
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
