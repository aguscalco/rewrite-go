package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class MathLog1p extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Log1p";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Log(1 + x)` or `math.Log(x + 1)` to `math.Log1p(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (isMathLog(c)) {
                    Expr arg = c.getArgs().get(0);
                    if (arg instanceof BinaryExpr) {
                        BinaryExpr b = (BinaryExpr) arg;
                        if ("+".equals(b.getOp())) {
                            if (isOne(b.getX())) {
                                return toLog1p(c, b.getY());
                            } else if (isOne(b.getY())) {
                                return toLog1p(c, b.getX());
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isMathLog(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                        return "Log".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
            
            private boolean isOne(Expr expr) {
                if (expr instanceof BasicLit) {
                    return "1".equals(((BasicLit) expr).getValue()) || "1.0".equals(((BasicLit) expr).getValue());
                }
                return false;
            }
            
            private Tree toLog1p(CallExpr logCall, Expr arg) {
                SelectorExpr sel = (SelectorExpr) logCall.getFun();
                Ident log1pIdent = sel.getSel().withName("Log1p");
                return logCall.withFun(sel.withSel(log1pIdent)).withArgs(Collections.singletonList(arg.withPrefix(Space.EMPTY)));
            }
        };
    }
}
