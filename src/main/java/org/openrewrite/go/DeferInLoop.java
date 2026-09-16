package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class DeferInLoop extends Recipe {

    @Override
    public String getDisplayName() {
        return "Detect defer statements in loops";
    }

    @Override
    public String getDescription() {
        return "Flags defer statements inside loops, which can cause memory leaks and resource exhaustion since they do not execute until the surrounding function returns.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public DeferStmt visitDeferStmt(DeferStmt deferStmt, ExecutionContext ctx) {
                DeferStmt d = (DeferStmt) super.visitDeferStmt(deferStmt, ctx);

                boolean inFor = getCursor().firstEnclosing(ForStmt.class) != null || 
                                getCursor().firstEnclosing(RangeStmt.class) != null;
                                
                if (inFor) {
                    // Check if it's wrapped in a FuncLit (closure) which is safe
                    boolean insideFuncLit = false;
                    
                    // We check if there's a FuncLit between the defer and the loop
                    org.openrewrite.Cursor c = getCursor();
                    while (c != null) {
                        if (c.getValue() instanceof FuncLit) {
                            insideFuncLit = true;
                            break;
                        }
                        if (c.getValue() instanceof ForStmt || c.getValue() instanceof RangeStmt) {
                            break;
                        }
                        c = c.getParent();
                    }
                    
                    if (!insideFuncLit) {
                        return SearchResult.found(d, "Resource leak warning: defer inside a loop won't execute until the enclosing function returns. Wrap it in a closure (func() { ... })() to execute it per iteration.");
                    }
                }

                return d;
            }
        };
    }
}
