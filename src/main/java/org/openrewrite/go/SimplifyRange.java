package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class SimplifyRange extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify range loops";
    }

    @Override
    public String getDescription() {
        return "Removes redundant blank identifiers `_` in range loops. Converts `for k, _ := range m` to `for k := range m` matching golangci-lint's `simplify` check.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public RangeStmt visitRangeStmt(RangeStmt rangeStmt, ExecutionContext ctx) {
                RangeStmt r = (RangeStmt) super.visitRangeStmt(rangeStmt, ctx);

                if (r.getValue() instanceof Ident && "_".equals(((Ident) r.getValue()).getName())) {
                    return r.withValue(null);
                }

                return r;
            }
        };
    }
}
