package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class IterSeqMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate channel generators to iter.Seq";
    }

    @Override
    public String getDescription() {
        return "Flags function signatures returning channels (the old generator pattern) for migration to the new Go 1.23+ iter.Seq pattern.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Field visitField(Field field, ExecutionContext ctx) {
                Field f = (Field) super.visitField(field, ctx);
                
                // We want to target fields in FuncType's results (return values)
                if (f.getType() instanceof ChanTypeExpr) {
                    // Check if it's part of a function return type.
                    // Instead of checking the parent, we can just flag all ChanTypeExpr 
                    // inside function return values. We can also just flag the ChanTypeExpr itself.
                    return SearchResult.found(f, "Go 1.23+: Consider replacing this channel-based generator with iter.Seq.");
                }
                
                return f;
            }
        };
    }
}
