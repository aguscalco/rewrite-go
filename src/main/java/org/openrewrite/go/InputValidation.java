package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Adds standard nil checks for pointer arguments on exported functions.
 */
public class InputValidation extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add input validation";
    }

    @Override
    public String getDescription() {
        return "Adds standard nil checks for pointer arguments on exported functions.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getName() == null || f.getName().getName().isEmpty()) {
                    return f;
                }
                
                String funcName = f.getName().getName();
                if (!Character.isUpperCase(funcName.charAt(0))) {
                    // Only target exported functions
                    return f;
                }

                if (f.getType() == null || f.getType().getParams() == null || f.getBody() == null) {
                    return f;
                }

                List<Stmt> newStmts = new ArrayList<>();
                boolean changed = false;

                for (Field param : f.getType().getParams()) {
                    if (param.getType() instanceof StarExpr && param.getNames() != null && !param.getNames().isEmpty()) {
                        String paramName = param.getNames().get(0);

                        // Check if a nil check already exists for this param
                        if (!hasNilCheck(f.getBody().getStmts(), paramName)) {
                            newStmts.add(createNilCheck(paramName));
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    newStmts.addAll(f.getBody().getStmts());
                    return f.withBody(f.getBody().withStmts(newStmts));
                }

                return f;
            }

            private boolean hasNilCheck(List<Stmt> stmts, String paramName) {
                if (stmts == null || stmts.isEmpty()) {
                    return false;
                }
                // Very basic heuristic: check if the first few statements involve paramName and "nil"
                // A robust implementation would deeply inspect IfStmt conditions.
                for (Stmt stmt : stmts) {
                    if (stmt instanceof IfStmt) {
                        IfStmt ifStmt = (IfStmt) stmt;
                        if (ifStmt.getCond() instanceof BinaryExpr) {
                            BinaryExpr bin = (BinaryExpr) ifStmt.getCond();
                            if ("==".equals(bin.getOp()) && bin.getX() instanceof Ident && bin.getY() instanceof Ident) {
                                Ident x = (Ident) bin.getX();
                                Ident y = (Ident) bin.getY();
                                if ((x.getName().equals(paramName) && y.getName().equals("nil")) ||
                                    (y.getName().equals(paramName) && x.getName().equals("nil"))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            private IfStmt createNilCheck(String paramName) {
                Ident paramIdent = new Ident(Tree.randomId(), Space.build(" "), Markers.EMPTY, paramName, null);
                Ident nilIdent = new Ident(Tree.randomId(), Space.build(" "), Markers.EMPTY, "nil", null);
                
                BinaryExpr cond = new BinaryExpr(
                        Tree.randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        paramIdent,
                        "==",
                        nilIdent,
                        null
                );

                Ident panicIdent = new Ident(Tree.randomId(), Space.build("\n\t\t"), Markers.EMPTY, "panic", null);
                BasicLit msgLit = new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"" + paramName + " cannot be nil\"");
                CallExpr panicCall = new CallExpr(
                        Tree.randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        panicIdent,
                        Collections.singletonList(msgLit),
                        false,
                        null
                );

                ExprStmt panicStmt = new ExprStmt(Tree.randomId(), Space.EMPTY, Markers.EMPTY, panicCall);

                BlockStmt body = new BlockStmt(
                        Tree.randomId(),
                        Space.build(" "),
                        Markers.EMPTY,
                        Collections.singletonList(panicStmt),
                        Space.build("\n\t")
                );

                return new IfStmt(
                        Tree.randomId(),
                        Space.build("\n\t"),
                        Markers.EMPTY,
                        null,
                        cond,
                        body,
                        null
                );
            }
        };
    }
}
