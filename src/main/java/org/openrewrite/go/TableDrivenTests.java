package org.openrewrite.go;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class TableDrivenTests extends Recipe {

    @Override
    public String getDisplayName() {
        return "Find sequential tests for table-driven refactoring";
    }

    @Override
    public String getDescription() {
        return "Flags consecutive t.Run calls that could be refactored into idiomatic table-driven tests.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);

                if (b.getStmts() != null && b.getStmts().size() >= 2) {
                    boolean consecutiveRuns = false;
                    for (int i = 0; i < b.getStmts().size() - 1; i++) {
                        Stmt s1 = b.getStmts().get(i);
                        Stmt s2 = b.getStmts().get(i + 1);
                        
                        if (isTRun(s1) && isTRun(s2)) {
                            consecutiveRuns = true;
                            break;
                        }
                    }
                    
                    if (consecutiveRuns) {
                        return SearchResult.found(b, "Consider refactoring these sequential t.Run calls into a table-driven test");
                    }
                }

                return b;
            }
            
            private boolean isTRun(Stmt stmt) {
                if (stmt instanceof ExprStmt) {
                    Expr expr = ((ExprStmt) stmt).getExpr();
                    if (expr instanceof CallExpr) {
                        CallExpr call = (CallExpr) expr;
                        if (call.getFun() instanceof SelectorExpr) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            return sel.getX() instanceof Ident && "Run".equals(sel.getSel().getName());
                        }
                    }
                }
                return false;
            }
        };
    }
}
