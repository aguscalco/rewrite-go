package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathAbsNeg extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove unnecessary negation in math.Abs()";
    }

    @Override
    public String getDescription() {
        return "Simplifies `math.Abs(-x)` to `math.Abs(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName()) && "Abs".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof UnaryExpr) {
                            UnaryExpr u = (UnaryExpr) arg;
                            if ("-".equals(u.getOp())) {
                                return c.withArgs(java.util.Collections.singletonList(u.getX().withPrefix(u.getPrefix())));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
