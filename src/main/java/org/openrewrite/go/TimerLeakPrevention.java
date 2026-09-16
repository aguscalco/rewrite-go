package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class TimerLeakPrevention extends Recipe {

    @Override
    public String getDisplayName() {
        return "Prevent time.After memory leaks in loops";
    }

    @Override
    public String getDescription() {
        return "Flags time.After calls inside select loops which can cause memory leaks if the loop continues, recommending time.NewTimer instead.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "time".equals(((Ident) sel.getX()).getName())) {
                        if ("After".equals(sel.getSel().getName())) {
                            boolean inFor = getCursor().firstEnclosing(ForStmt.class) != null || 
                                            getCursor().firstEnclosing(RangeStmt.class) != null;
                            
                            if (inFor) {
                                return SearchResult.found(c, "Memory leak warning: time.After inside a loop will not be garbage collected until the timer expires. Use time.NewTimer and call timer.Stop() instead.");
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
