package org.openrewrite.go.tree;

import org.openrewrite.Cursor;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

public class GoPrinter<P> extends TreeVisitor<Tree, PrintOutputCapture<P>> {
    
    @Override
    public Tree visit(Tree tree, PrintOutputCapture<P> p) {
        if (tree instanceof Go) {
            return ((Go) tree).accept(new GoVisitor<PrintOutputCapture<P>>() {
                @Override
                public Tree visitGoFile(GoFile goFile, PrintOutputCapture<P> p) {
                    printSpace(goFile.getPrefix(), p);
                    if (goFile.getPackageClause() != null) {
                        visit(goFile.getPackageClause(), p);
                    }
                    for (ImportDecl imp : goFile.getImports()) {
                        visit(imp, p);
                    }
                    for (Decl decl : goFile.getDeclarations()) {
                        visit(decl, p);
                    }
                    printSpace(goFile.getEof(), p);
                    return goFile;
                }
                
                @Override
                public Tree visitPackageClause(PackageClause packageClause, PrintOutputCapture<P> p) {
                    printSpace(packageClause.getPrefix(), p);
                    p.out.append("package ");
                    visit(packageClause.getName(), p);
                    return packageClause;
                }
                
                @Override
                public Tree visitImportDecl(ImportDecl importDecl, PrintOutputCapture<P> p) {
                    printSpace(importDecl.getPrefix(), p);
                    p.out.append("import ");
                    if (importDecl.isGrouped()) {
                        p.out.append("(");
                        for (ImportSpec spec : importDecl.getSpecs()) {
                            visit(spec, p);
                        }
                        p.out.append(")");
                    } else if (!importDecl.getSpecs().isEmpty()) {
                        visit(importDecl.getSpecs().get(0), p);
                    }
                    printSpace(importDecl.getEnd(), p);
                    return importDecl;
                }
                
                @Override
                public Tree visitImportSpec(ImportSpec importSpec, PrintOutputCapture<P> p) {
                    printSpace(importSpec.getPrefix(), p);
                    if (importSpec.getAlias() != null) {
                        visit(importSpec.getAlias(), p);
                        p.out.append(" ");
                    }
                    visit(importSpec.getPath(), p);
                    return importSpec;
                }
                
                @Override
                public Tree visitIdent(Ident ident, PrintOutputCapture<P> p) {
                    printSpace(ident.getPrefix(), p);
                    p.out.append(ident.getName());
                    return ident;
                }
                
                @Override
                public Tree visitBasicLit(BasicLit basicLit, PrintOutputCapture<P> p) {
                    printSpace(basicLit.getPrefix(), p);
                    p.out.append(basicLit.getValue());
                    return basicLit;
                }
                
                @Override
                public Tree visitFuncDecl(FuncDecl funcDecl, PrintOutputCapture<P> p) {
                    printSpace(funcDecl.getPrefix(), p);
                    p.out.append("func ");
                    if (funcDecl.getRecv() != null) {
                        p.out.append("(");
                        visit(funcDecl.getRecv(), p);
                        p.out.append(") ");
                    }
                    visit(funcDecl.getName(), p);
                    if (funcDecl.getType() != null) {
                        visit(funcDecl.getType(), p);
                    }
                    if (funcDecl.getBody() != null) {
                        p.out.append(" ");
                        visit(funcDecl.getBody(), p);
                    }
                    return funcDecl;
                }
                
                @Override
                public Tree visitGenDecl(GenDecl genDecl, PrintOutputCapture<P> p) {
                    printSpace(genDecl.getPrefix(), p);
                    p.out.append(genDecl.getTok()).append(" ");
                    if (genDecl.isGrouped()) {
                        p.out.append("(");
                        for (Spec spec : genDecl.getSpecs()) {
                            visit(spec, p);
                        }
                        p.out.append(")");
                    } else if (!genDecl.getSpecs().isEmpty()) {
                        visit(genDecl.getSpecs().get(0), p);
                    }
                    printSpace(genDecl.getEnd(), p);
                    return genDecl;
                }
                
                @Override
                public Tree visitValueSpec(ValueSpec valueSpec, PrintOutputCapture<P> p) {
                    printSpace(valueSpec.getPrefix(), p);
                    for (int i = 0; i < valueSpec.getNames().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        visit(valueSpec.getNames().get(i), p);
                    }
                    if (valueSpec.getType() != null) {
                        p.out.append(" ");
                        visit(valueSpec.getType(), p);
                    }
                    if (!valueSpec.getValues().isEmpty()) {
                        p.out.append(" = ");
                        for (int i = 0; i < valueSpec.getValues().size(); i++) {
                            if (i > 0) {
                                p.out.append(", ");
                            }
                            visit(valueSpec.getValues().get(i), p);
                        }
                    }
                    return valueSpec;
                }
                
                @Override
                public Tree visitTypeSpec(TypeSpec typeSpec, PrintOutputCapture<P> p) {
                    printSpace(typeSpec.getPrefix(), p);
                    visit(typeSpec.getName(), p);
                    if (typeSpec.isAssign()) {
                        p.out.append(" =");
                    }
                    p.out.append(" ");
                    visit(typeSpec.getType(), p);
                    return typeSpec;
                }
                
                @Override
                public Tree visitField(Field field, PrintOutputCapture<P> p) {
                    printSpace(field.getPrefix(), p);
                    for (int i = 0; i < field.getNames().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        p.out.append(field.getNames().get(i));
                    }
                    if (field.getType() != null) {
                        p.out.append(" ");
                        visit(field.getType(), p);
                    }
                    if (field.getTag() != null && !field.getTag().isEmpty()) {
                        p.out.append(" ").append(field.getTag());
                    }
                    return field;
                }
                
                @Override
                public Tree visitBlockStmt(BlockStmt blockStmt, PrintOutputCapture<P> p) {
                    printSpace(blockStmt.getPrefix(), p);
                    p.out.append("{");
                    for (Stmt stmt : blockStmt.getStmts()) {
                        visit(stmt, p);
                    }
                    p.out.append("}");
                    printSpace(blockStmt.getEnd(), p);
                    return blockStmt;
                }
                
                @Override
                public Tree visitExprStmt(ExprStmt exprStmt, PrintOutputCapture<P> p) {
                    printSpace(exprStmt.getPrefix(), p);
                    visit(exprStmt.getExpr(), p);
                    return exprStmt;
                }
                
                @Override
                public Tree visitAssignStmt(AssignStmt assignStmt, PrintOutputCapture<P> p) {
                    printSpace(assignStmt.getPrefix(), p);
                    for (int i = 0; i < assignStmt.getLhs().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        visit(assignStmt.getLhs().get(i), p);
                    }
                    p.out.append(" ").append(assignStmt.getTok()).append(" ");
                    for (int i = 0; i < assignStmt.getRhs().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        visit(assignStmt.getRhs().get(i), p);
                    }
                    return assignStmt;
                }
                
                @Override
                public Tree visitReturnStmt(ReturnStmt returnStmt, PrintOutputCapture<P> p) {
                    printSpace(returnStmt.getPrefix(), p);
                    p.out.append("return");
                    if (!returnStmt.getResults().isEmpty()) {
                        p.out.append(" ");
                        for (int i = 0; i < returnStmt.getResults().size(); i++) {
                            if (i > 0) {
                                p.out.append(", ");
                            }
                            visit(returnStmt.getResults().get(i), p);
                        }
                    }
                    return returnStmt;
                }
                
                @Override
                public Tree visitCallExpr(CallExpr callExpr, PrintOutputCapture<P> p) {
                    printSpace(callExpr.getPrefix(), p);
                    visit(callExpr.getFun(), p);
                    p.out.append("(");
                    for (int i = 0; i < callExpr.getArgs().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        visit(callExpr.getArgs().get(i), p);
                    }
                    if (callExpr.isEllipsis()) {
                        p.out.append("...");
                    }
                    p.out.append(")");
                    return callExpr;
                }
                
                @Override
                public Tree visitSelectorExpr(SelectorExpr selectorExpr, PrintOutputCapture<P> p) {
                    printSpace(selectorExpr.getPrefix(), p);
                    visit(selectorExpr.getX(), p);
                    p.out.append(".");
                    visit(selectorExpr.getSel(), p);
                    return selectorExpr;
                }
                
                @Override
                public Tree visitBinaryExpr(BinaryExpr binaryExpr, PrintOutputCapture<P> p) {
                    printSpace(binaryExpr.getPrefix(), p);
                    visit(binaryExpr.getX(), p);
                    p.out.append(" ").append(binaryExpr.getOp()).append(" ");
                    visit(binaryExpr.getY(), p);
                    return binaryExpr;
                }
                
                @Override
                public Tree visitUnaryExpr(UnaryExpr unaryExpr, PrintOutputCapture<P> p) {
                    printSpace(unaryExpr.getPrefix(), p);
                    p.out.append(unaryExpr.getOp());
                    visit(unaryExpr.getX(), p);
                    return unaryExpr;
                }
                
                @Override
                public Tree visitFuncType(FuncType funcType, PrintOutputCapture<P> p) {
                    printSpace(funcType.getPrefix(), p);
                    p.out.append("(");
                    for (int i = 0; i < funcType.getParams().size(); i++) {
                        if (i > 0) {
                            p.out.append(", ");
                        }
                        visit(funcType.getParams().get(i), p);
                    }
                    p.out.append(")");
                    if (!funcType.getResults().isEmpty()) {
                        p.out.append(" ");
                        if (funcType.getResults().size() == 1 && funcType.getResults().get(0).getNames().isEmpty()) {
                            visit(funcType.getResults().get(0).getType(), p);
                        } else {
                            p.out.append("(");
                            for (int i = 0; i < funcType.getResults().size(); i++) {
                                if (i > 0) {
                                    p.out.append(", ");
                                }
                                visit(funcType.getResults().get(i), p);
                            }
                            p.out.append(")");
                        }
                    }
                    return funcType;
                }
                
                @Override
                public Tree visitGoType(GoType goType, PrintOutputCapture<P> p) {
                    printSpace(goType.getPrefix(), p);
                    printGoType(goType.getType(), p);
                    return goType;
                }
                
                private void printGoType(GoType.Type type, PrintOutputCapture<P> p) {
                    if (type instanceof GoType.Basic) {
                        p.out.append(((GoType.Basic) type).getKind());
                    } else if (type instanceof GoType.Named) {
                        GoType.Named named = (GoType.Named) type;
                        if (named.getPackagePath() != null && !named.getPackagePath().isEmpty()) {
                            String[] parts = named.getPackagePath().split("/");
                            p.out.append(parts[parts.length - 1]).append(".");
                        }
                        p.out.append(named.getName());
                    } else if (type instanceof GoType.Pointer) {
                        p.out.append("*");
                        visit(((GoType.Pointer) type).getElem(), p);
                    } else if (type instanceof GoType.Slice) {
                        p.out.append("[]");
                        visit(((GoType.Slice) type).getElem(), p);
                    } else if (type instanceof GoType.Array) {
                        GoType.Array array = (GoType.Array) type;
                        p.out.append("[").append(String.valueOf(array.getLen())).append("]");
                        visit(array.getElem(), p);
                    } else if (type instanceof GoType.Map) {
                        GoType.Map map = (GoType.Map) type;
                        p.out.append("map[");
                        visit(map.getKey(), p);
                        p.out.append("]");
                        visit(map.getValue(), p);
                    }
                }
            }, p);
        }
        return tree;
    }
    
    private void printSpace(Space space, PrintOutputCapture<P> p) {
        if (space != null) {
            p.out.append(space.getWhitespace());
        }
    }
}
