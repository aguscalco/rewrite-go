package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class MathPow10 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Pow10";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Pow(10, x)` to `math.Pow10(x)`.";
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
                        
                        if (isTen(arg1)) {
                            Ident pow10Ident = sel.getSel().withName("Pow10");
                            return c.withFun(sel.withSel(pow10Ident)).withArgs(Collections.singletonList(arg2.withPrefix(arg1.getPrefix())));
                        }
                    }
                }

                return c;
            }

            private boolean isTen(Expr expr) {
                if (expr instanceof BasicLit) {
                    String val = ((BasicLit) expr).getValue();
                    return "10".equals(val) || "10.0".equals(val);
                }
                return false;
            }
        };
    }
}
