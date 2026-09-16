package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class MathFloorAddHalf extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Round instead of math.Floor(x + 0.5)";
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
                        if (arg instanceof BinaryExpr) {
                            BinaryExpr b = (BinaryExpr) arg;
                            if ("+".equals(b.getOp())) {
                                if (isHalf(b.getY())) {
                                    return toRound(c, b.getX());
                                } else if (isHalf(b.getX())) {
                                    return toRound(c, b.getY());
                                }
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isHalf(Expr expr) {
                if (expr instanceof BasicLit) {
                    return "0.5".equals(((BasicLit) expr).getValue());
                }
                return false;
            }
            
            private Tree toRound(CallExpr call, Expr xArg) {
                SelectorExpr sel = (SelectorExpr) call.getFun();
                Ident roundIdent = sel.getSel().withName("Round");
                return call.withFun(sel.withSel(roundIdent)).withArgs(Collections.singletonList(xArg.withPrefix(Space.EMPTY)));
            }
        };
    }
}
