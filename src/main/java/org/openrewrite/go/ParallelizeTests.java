package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class ParallelizeTests extends Recipe {

    @Override
    public String getDisplayName() {
        return "Suggest parallelizing test execution";
    }

    @Override
    public String getDescription() {
        return "Flags t.Run() subtests that do not call t.Parallel(), recommending parallel execution to speed up test suites.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && ("t".equals(((Ident) sel.getX()).getName()) || "b".equals(((Ident) sel.getX()).getName()))) {
                        if ("Run".equals(sel.getSel().getName())) {
                            
                            // Check if it has a func literal as the second argument
                            if (c.getArgs() != null && c.getArgs().size() == 2) {
                                Expr arg2 = c.getArgs().get(1);
                                if (arg2 instanceof FuncLit) {
                                    FuncLit funcLit = (FuncLit) arg2;
                                    boolean hasParallel = false;
                                    
                                    if (funcLit.getBody() != null && funcLit.getBody().getStmts() != null) {
                                        for (Stmt stmt : funcLit.getBody().getStmts()) {
                                            if (stmt instanceof ExprStmt) {
                                                Expr expr = ((ExprStmt) stmt).getExpr();
                                                if (expr instanceof CallExpr) {
                                                    CallExpr innerCall = (CallExpr) expr;
                                                    if (innerCall.getFun() instanceof SelectorExpr) {
                                                        SelectorExpr innerSel = (SelectorExpr) innerCall.getFun();
                                                        if ("Parallel".equals(innerSel.getSel().getName())) {
                                                            hasParallel = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (!hasParallel) {
                                            return SearchResult.found(c, "Consider adding t.Parallel() at the start of this subtest to improve test suite performance.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
