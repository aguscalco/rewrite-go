package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class UseStringBuilder extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.Builder";
    }

    @Override
    public String getDescription() {
        return "Flags string concatenation within loops for manual conversion to strings.Builder.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public AssignStmt visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = (AssignStmt) super.visitAssignStmt(assignStmt, ctx);

                if ("+=".equals(a.getTok()) && a.getLhs() != null && !a.getLhs().isEmpty()) {
                    if (isInsideLoop(getCursor())) {
                        return SearchResult.found(a, "Performance: Consider using strings.Builder instead of string concatenation (+|) in loops");
                    }
                }
                
                if ("=".equals(a.getTok()) && a.getRhs() != null && !a.getRhs().isEmpty()) {
                    if (a.getRhs().get(0) instanceof BinaryExpr) {
                        BinaryExpr bin = (BinaryExpr) a.getRhs().get(0);
                        if (isInsideLoop(getCursor())) {
                            // Can't reliably know it's a string without type info, but we flag it if it's += or potentially string
                        }
                    }
                }

                return a;
            }

            private boolean isInsideLoop(org.openrewrite.Cursor cursor) {
                return cursor.dropParentUntil(p -> p instanceof ForStmt || p instanceof RangeStmt || p instanceof FuncDecl).getValue() instanceof RangeStmt
                    || cursor.dropParentUntil(p -> p instanceof ForStmt || p instanceof RangeStmt || p instanceof FuncDecl).getValue() instanceof ForStmt;
            }
        };
    }
}
