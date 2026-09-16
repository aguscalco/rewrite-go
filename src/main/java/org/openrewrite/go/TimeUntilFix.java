package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class TimeUntilFix extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use time.Until";
    }

    @Override
    public String getDescription() {
        return "Converts `t.Sub(time.Now())` to `time.Until(t)` for better readability, matching gocritic's timeExpr check.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    
                    if ("Sub".equals(sel.getSel().getName()) && c.getArgs() != null && c.getArgs().size() == 1) {
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof CallExpr) {
                            CallExpr argCall = (CallExpr) arg;
                            if (argCall.getFun() instanceof SelectorExpr) {
                                SelectorExpr argSel = (SelectorExpr) argCall.getFun();
                                if ("Now".equals(argSel.getSel().getName()) && argSel.getX() instanceof Ident) {
                                    if ("time".equals(((Ident) argSel.getX()).getName())) {
                                        // Found t.Sub(time.Now())
                                        // Change to time.Until(t)
                                        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
                                        Ident untilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Until", null);
                                        SelectorExpr untilSel = new SelectorExpr(UUID.randomUUID(), c.getFun().getPrefix(), Markers.EMPTY, timeIdent, untilIdent, null);
                                        
                                        return c.withFun(untilSel).withArgs(Collections.singletonList(sel.getX()));
                                    }
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
