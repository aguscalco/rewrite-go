package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class UnnecessaryStringCast extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove unnecessary string casts";
    }

    @Override
    public String getDescription() {
        return "Removes `string(\"...\")` around string literals since they are already strings.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof Ident && "string".equals(((Ident) c.getFun()).getName())) {
                    if (c.getArgs() != null && c.getArgs().size() == 1) {
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BasicLit && "STRING".equals(((BasicLit) arg).getKind())) {
                            return arg.withPrefix(c.getPrefix());
                        }
                    }
                }

                return c;
            }
        };
    }
}
