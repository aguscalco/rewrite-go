package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class MathPowToMultiplication extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use multiplication over math.Pow(x, 2)";
    }

    @Override
    public String getDescription() {
        return "Replaces `math.Pow(x, 2)` with `x * x` for significantly better performance.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName()) && "Pow".equals(sel.getSel().getName())) {
                        
                        Expr exponent = c.getArgs().get(1);
                        if (exponent instanceof BasicLit && "INT".equals(((BasicLit) exponent).getKind()) && "2".equals(((BasicLit) exponent).getValue())) {
                            Expr base = c.getArgs().get(0);
                            return new BinaryExpr(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, base.withPrefix(Space.EMPTY), "*", base.withPrefix(Space.build(" ")), null);
                        }
                    }
                }

                return c;
            }
        };
    }
}
