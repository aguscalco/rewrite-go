package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathIsNaNCompare extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.IsNaN instead of == math.NaN()";
    }

    @Override
    public String getDescription() {
        return "Migrates `x == math.NaN()` to `math.IsNaN(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp())) {
                    if (isMathNaN(b.getY())) {
                        return createIsNaN(b.getX(), b.getPrefix());
                    } else if (isMathNaN(b.getX())) {
                        return createIsNaN(b.getY(), b.getPrefix());
                    }
                }

                return b;
            }

            private boolean isMathNaN(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                            return "NaN".equals(sel.getSel().getName());
                        }
                    }
                }
                return false;
            }
            
            private Tree createIsNaN(Expr x, Space prefix) {
                Ident mathIdent = new Ident(java.util.UUID.randomUUID(), Space.EMPTY, org.openrewrite.marker.Markers.EMPTY, "math", null);
                Ident isNaNIdent = new Ident(java.util.UUID.randomUUID(), Space.EMPTY, org.openrewrite.marker.Markers.EMPTY, "IsNaN", null);
                SelectorExpr sel = new SelectorExpr(java.util.UUID.randomUUID(), Space.EMPTY, org.openrewrite.marker.Markers.EMPTY, mathIdent, isNaNIdent, null);
                
                return new CallExpr(java.util.UUID.randomUUID(), prefix, org.openrewrite.marker.Markers.EMPTY, sel, java.util.Collections.singletonList(x.withPrefix(Space.EMPTY)), false, null);
            }
        };
    }
}
