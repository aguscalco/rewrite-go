package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrates os.Setenv(k, v) inside tests to t.Setenv(k, v).
 * Also removes corresponding defer os.Unsetenv(k) calls if present.
 */
public class UseTSetenv extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate os.Setenv to t.Setenv";
    }

    @Override
    public String getDescription() {
        return "Migrates os.Setenv inside tests to t.Setenv (introduced in Go 1.17), removing the need for manual cleanup.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            private String testingVarName = null;

            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                String oldTestingVar = testingVarName;
                testingVarName = null;

                // Check if this function takes *testing.T
                if (funcDecl.getType() != null && funcDecl.getType().getParams() != null) {
                    for (Field param : funcDecl.getType().getParams()) {
                        if (param.getType() instanceof StarExpr) {
                            StarExpr star = (StarExpr) param.getType();
                            if (star.getX() instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) star.getX();
                                if (sel.getX() instanceof Ident && "testing".equals(((Ident) sel.getX()).getName()) &&
                                    "T".equals(sel.getSel().getName())) {
                                    
                                    if (param.getNames() != null && !param.getNames().isEmpty()) {
                                        testingVarName = param.getNames().get(0);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (testingVarName == null) {
                    return funcDecl;
                }

                Tree result = super.visitFuncDecl(funcDecl, ctx);
                testingVarName = oldTestingVar;
                return result;
            }

            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (testingVarName != null && c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "os".equals(((Ident) sel.getX()).getName())) {
                        if ("Setenv".equals(sel.getSel().getName())) {
                            Ident tIdent = new Ident(Tree.randomId(), ((Ident)sel.getX()).getPrefix(), Markers.EMPTY, testingVarName, null);
                            Ident setenvIdent = sel.getSel();
                            SelectorExpr newSel = sel.withX(tIdent).withSel(setenvIdent);
                            return c.withFun(newSel);
                        }
                    }
                }

                return c;
            }
            
            @Override
            public Tree visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);
                
                if (testingVarName != null && b.getStmts() != null) {
                    List<Stmt> newStmts = new ArrayList<>();
                    boolean changed = false;
                    for (Stmt stmt : b.getStmts()) {
                        // Check for defer os.Unsetenv
                        if (stmt instanceof DeferStmt) {
                            DeferStmt defer = (DeferStmt) stmt;
                            if (defer.getCall() instanceof CallExpr) {
                                CallExpr call = (CallExpr) defer.getCall();
                                if (call.getFun() instanceof SelectorExpr) {
                                    SelectorExpr sel = (SelectorExpr) call.getFun();
                                    if (sel.getX() instanceof Ident && "os".equals(((Ident) sel.getX()).getName()) &&
                                        "Unsetenv".equals(sel.getSel().getName())) {
                                        changed = true;
                                        continue; // skip adding this stmt
                                    }
                                }
                            }
                        }
                        newStmts.add(stmt);
                    }
                    if (changed) {
                        return b.withStmts(newStmts);
                    }
                }
                
                return b;
            }
        };
    }
}
