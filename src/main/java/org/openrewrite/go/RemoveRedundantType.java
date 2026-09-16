package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class RemoveRedundantType extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove redundant type in composite literals";
    }

    @Override
    public String getDescription() {
        return "Removes redundant type declarations in nested composite literals, making the code cleaner (e.g. `[]T{{...}}` instead of `[]T{T{...}}`). Matches golangci-lint's `simplify` check.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CompositeLit visitCompositeLit(CompositeLit compositeLit, ExecutionContext ctx) {
                CompositeLit c = (CompositeLit) super.visitCompositeLit(compositeLit, ctx);

                if (c.getType() != null && getCursor().getParent() != null && getCursor().getParent().getValue() instanceof CompositeLit) {
                    CompositeLit parent = getCursor().getParent().getValue();
                    if (parent.getType() instanceof SliceTypeExpr || parent.getType() instanceof ArrayTypeExpr || parent.getType() instanceof MapTypeExpr) {
                        return c.withType(null);
                    }
                }

                return c;
            }
        };
    }
}
