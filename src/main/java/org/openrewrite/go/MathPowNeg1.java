package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class MathPowNeg1 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use 1.0 / x instead of math.Pow(x, -1)";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Pow(x, -1)` to `1.0 / x`.";
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
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        if (isMinusOne(arg2)) {
                            BasicLit oneLit = new BasicLit(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, "FLOAT", "1.0");
                            return new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, oneLit, "/", arg1.withPrefix(Space.build(" ")), null);
                        }
                    }
                }

                return c;
            }

            private boolean isMinusOne(Expr expr) {
                if (expr instanceof UnaryExpr) {
                    UnaryExpr unary = (UnaryExpr) expr;
                    if ("-".equals(unary.getOp()) && unary.getX() instanceof BasicLit) {
                        String val = ((BasicLit) unary.getX()).getValue();
                        return "1".equals(val) || "1.0".equals(val);
                    }
                }
                return false;
            }
        };
    }
}
