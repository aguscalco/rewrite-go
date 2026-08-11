package org.openrewrite.go.tree;

import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

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
        return visitGo(goFile, p);
    }
    
    public Tree visitPackageClause(PackageClause packageClause, P p) {
        return visitGo(packageClause, p);
    }
    
    public Tree visitImportDecl(ImportDecl importDecl, P p) {
        return visitGo(importDecl, p);
    }
    
    public Tree visitImportSpec(ImportSpec importSpec, P p) {
        return visitGo(importSpec, p);
    }
    
    public Tree visitIdent(Ident ident, P p) {
        return visitGo(ident, p);
    }
    
    public Tree visitBasicLit(BasicLit basicLit, P p) {
        return visitGo(basicLit, p);
    }
    
    public Tree visitFuncDecl(FuncDecl funcDecl, P p) {
        return visitGo(funcDecl, p);
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
        return visitGo(blockStmt, p);
    }
    
    public Tree visitExprStmt(ExprStmt exprStmt, P p) {
        return visitGo(exprStmt, p);
    }
    
    public Tree visitAssignStmt(AssignStmt assignStmt, P p) {
        return visitGo(assignStmt, p);
    }
    
    public Tree visitReturnStmt(ReturnStmt returnStmt, P p) {
        return visitGo(returnStmt, p);
    }
    
    public Tree visitCallExpr(CallExpr callExpr, P p) {
        return visitGo(callExpr, p);
    }
    
    public Tree visitSelectorExpr(SelectorExpr selectorExpr, P p) {
        return visitGo(selectorExpr, p);
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
