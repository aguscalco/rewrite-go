package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class UseSyncPool extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use sync.Pool";
    }

    @Override
    public String getDescription() {
        return "Flags frequent struct allocations inside loops for sync.Pool optimization.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public AssignStmt visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = (AssignStmt) super.visitAssignStmt(assignStmt, ctx);

                if (":=".equals(a.getTok()) || "=".equals(a.getTok())) {
                    if (a.getRhs() != null && !a.getRhs().isEmpty()) {
                        Expr rhs = a.getRhs().get(0);
                        
                        // Look for `&MyStruct{}`
                        if (rhs instanceof UnaryExpr && "&".equals(((UnaryExpr) rhs).getOp())) {
                            if (((UnaryExpr) rhs).getX() instanceof CompositeLit) {
                                if (isInsideLoop(getCursor())) {
                                    return SearchResult.found(a, "Performance: Consider using sync.Pool for frequent allocations inside loops");
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
