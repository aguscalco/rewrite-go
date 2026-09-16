package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathExp2 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Exp2 over math.Pow";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Pow(2, x)` to `math.Exp2(x)` for better performance and readability.";
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
                        
                        Expr baseArg = c.getArgs().get(0);
                        if (baseArg instanceof BasicLit && "2".equals(((BasicLit) baseArg).getValue())) {
                            Ident exp2Ident = sel.getSel().withName("Exp2");
                            return c.withFun(sel.withSel(exp2Ident)).withArgs(java.util.Collections.singletonList(c.getArgs().get(1)));
                        }
                    }
                }

                return c;
            }
        };
    }
}
