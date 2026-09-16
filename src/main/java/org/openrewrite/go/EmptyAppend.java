package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class EmptyAppend extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove empty append() calls";
    }

    @Override
    public String getDescription() {
        return "Removes `append(s)` calls with no elements to append, reducing it directly to `s`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof Ident && "append".equals(((Ident) c.getFun()).getName())) {
                    if (c.getArgs() != null && c.getArgs().size() == 1) {
                        return c.getArgs().get(0).withPrefix(c.getPrefix());
                    }
                }

                return c;
            }
        };
    }
}
