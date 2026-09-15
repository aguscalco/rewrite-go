package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class UseBytesBuffer extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.Buffer";
    }

    @Override
    public String getDescription() {
        return "Flags repeated slice byte appending in loops for manual bytes.Buffer replacement.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public AssignStmt visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = (AssignStmt) super.visitAssignStmt(assignStmt, ctx);

                if ("=".equals(a.getTok())) {
                    if (a.getRhs() != null && !a.getRhs().isEmpty()) {
                        Expr rhs = a.getRhs().get(0);
                        
                        // Look for `b = append(b, ...)`
                        if (rhs instanceof CallExpr) {
                            CallExpr call = (CallExpr) rhs;
                            if (call.getFun() instanceof Ident && "append".equals(((Ident) call.getFun()).getName())) {
                                if (isInsideLoop(getCursor())) {
                                    return SearchResult.found(a, "Performance: Consider using bytes.Buffer instead of repeated byte slice appends in loops");
                                }
                            }
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
