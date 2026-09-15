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

/**
 * Adds t.Helper() to test helper functions to improve test failure output.
 */
public class AddTestHelper extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add t.Helper() to test helpers";
    }

    @Override
    public String getDescription() {
        return "Automatically injects t.Helper() at the beginning of functions that take a testing parameter but are not tests themselves.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getName() == null || f.getBody() == null) {
                    return f;
                }

                String funcName = f.getName().getName();
                if (funcName.startsWith("Test") || funcName.startsWith("Benchmark") || 
                    funcName.startsWith("Fuzz") || funcName.startsWith("Example")) {
                    return f; // Standard test functions don't need t.Helper()
                }

                String testingVarName = null;
                if (f.getType() != null && f.getType().getParams() != null) {
                    for (Field param : f.getType().getParams()) {
                        if (param.getType() instanceof StarExpr) {
                            StarExpr star = (StarExpr) param.getType();
                            if (star.getX() instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) star.getX();
                                if (sel.getX() instanceof Ident && "testing".equals(((Ident) sel.getX()).getName())) {
                                    String typeName = sel.getSel().getName();
                                    if ("T".equals(typeName) || "B".equals(typeName) || "F".equals(typeName) || "M".equals(typeName)) {
                                        if (param.getNames() != null && !param.getNames().isEmpty()) {
                                            testingVarName = param.getNames().get(0);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (testingVarName == null) {
                    return f;
                }

                List<Stmt> stmts = f.getBody().getStmts();
                if (stmts != null && !stmts.isEmpty()) {
                    Stmt firstStmt = stmts.get(0);
                    if (firstStmt instanceof ExprStmt) {
                        Expr expr = ((ExprStmt) firstStmt).getExpr();
                        if (expr instanceof CallExpr) {
                            CallExpr call = (CallExpr) expr;
                            if (call.getFun() instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) call.getFun();
                                if (sel.getX() instanceof Ident && testingVarName.equals(((Ident) sel.getX()).getName()) &&
                                    "Helper".equals(sel.getSel().getName())) {
                                    return f; // Already has t.Helper()
                                }
                            }
                        }
                    }
                }

                // Add t.Helper()
                Ident tIdent = new Ident(Tree.randomId(), Space.build("\n\t"), Markers.EMPTY, testingVarName, null);
                Ident helperIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "Helper", null);
                SelectorExpr helperSel = new SelectorExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, tIdent, helperIdent, null);
                CallExpr helperCall = new CallExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, helperSel, Collections.emptyList(), false, null);
                ExprStmt helperStmt = new ExprStmt(Tree.randomId(), Space.EMPTY, Markers.EMPTY, helperCall);

                List<Stmt> newStmts = new ArrayList<>();
                newStmts.add(helperStmt);
                if (stmts != null) {
                    newStmts.addAll(stmts);
                }

                return f.withBody(f.getBody().withStmts(newStmts));
            }
        };
    }
}
