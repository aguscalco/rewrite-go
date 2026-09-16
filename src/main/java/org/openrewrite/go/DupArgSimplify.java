package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;

public class DupArgSimplify extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify redundant duplicate arguments";
    }

    @Override
    public String getDescription() {
        return "Simplifies calls like `math.Max(a, a)` or `math.Min(a, a)` directly to `a`, matching gocritic's dupArg.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                        String method = sel.getSel().getName();
                        if ("Max".equals(method) || "Min".equals(method)) {
                            Expr arg1 = c.getArgs().get(0);
                            Expr arg2 = c.getArgs().get(1);
                            
                            // Naive equality for demonstration (e.g. both are Ident with same name)
                            if (arg1 instanceof Ident && arg2 instanceof Ident) {
                                if (((Ident) arg1).getName().equals(((Ident) arg2).getName())) {
                                    return arg1.withPrefix(c.getPrefix());
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
