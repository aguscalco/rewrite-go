package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class TimeSubNowToUntil extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use time.Until";
    }

    @Override
    public String getDescription() {
        return "Migrates `t.Sub(time.Now())` to `time.Until(t)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if ("Sub".equals(sel.getSel().getName())) {
                        if (isTimeNow(c.getArgs().get(0))) {
                            
                            Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
                            Ident untilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Until", null);
                            SelectorExpr untilSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, untilIdent, null);
                            
                            return c.withFun(untilSel).withArgs(Collections.singletonList(sel.getX().withPrefix(Space.EMPTY)));
                        }
                    }
                }

                return c;
            }

            private boolean isTimeNow(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr && (call.getArgs() == null || call.getArgs().isEmpty())) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "time".equals(((Ident) sel.getX()).getName())) {
                            return "Now".equals(sel.getSel().getName());
                        }
                    }
                }
                return false;
            }
        };
    }
}
