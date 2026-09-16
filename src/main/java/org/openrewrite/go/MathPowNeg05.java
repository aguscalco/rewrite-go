package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class MathPowNeg05 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use 1.0 / math.Sqrt(x) instead of math.Pow(x, -0.5)";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Pow(x, -0.5)` to `1.0 / math.Sqrt(x)`.";
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
                        
                        if (isMinusHalf(arg2)) {
                            BasicLit oneLit = new BasicLit(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, "FLOAT", "1.0");
                            
                            Ident sqrtIdent = sel.getSel().withName("Sqrt");
                            CallExpr sqrtCall = c.withFun(sel.withSel(sqrtIdent)).withArgs(Collections.singletonList(arg1.withPrefix(Space.EMPTY))).withPrefix(Space.build(" "));
                            
                            return new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, oneLit, "/", sqrtCall, null);
                        }
                    }
                }

                return c;
            }

            private boolean isMinusHalf(Expr expr) {
                if (expr instanceof UnaryExpr) {
                    UnaryExpr unary = (UnaryExpr) expr;
                    if ("-".equals(unary.getOp()) && unary.getX() instanceof BasicLit) {
                        String val = ((BasicLit) unary.getX()).getValue();
                        return "0.5".equals(val) || ".5".equals(val);
                    }
                }
                return false;
            }
        };
    }
}
