package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathFloorPlus05 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Round over math.Floor(x + 0.5)";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Floor(x + 0.5)` to `math.Round(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName()) && "Floor".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BinaryExpr && "+".equals(((BinaryExpr) arg).getOp())) {
                            BinaryExpr b = (BinaryExpr) arg;
                            if (b.getY() instanceof BasicLit && "0.5".equals(((BasicLit) b.getY()).getValue())) {
                                Ident roundIdent = sel.getSel().withName("Round");
                                return c.withFun(sel.withSel(roundIdent)).withArgs(java.util.Collections.singletonList(b.getX().withPrefix(c.getArgs().get(0).getPrefix())));
                            } else if (b.getX() instanceof BasicLit && "0.5".equals(((BasicLit) b.getX()).getValue())) {
                                Ident roundIdent = sel.getSel().withName("Round");
                                return c.withFun(sel.withSel(roundIdent)).withArgs(java.util.Collections.singletonList(b.getY().withPrefix(c.getArgs().get(0).getPrefix())));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
