package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class TimeSinceFix extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use time.Since";
    }

    @Override
    public String getDescription() {
        return "Converts `time.Now().Sub(t)` to `time.Since(t)` for better readability and performance, matching gocritic's timeExpr check.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    
                    if ("Sub".equals(sel.getSel().getName()) && sel.getX() instanceof CallExpr) {
                        CallExpr innerCall = (CallExpr) sel.getX();
                        if (innerCall.getFun() instanceof SelectorExpr) {
                            SelectorExpr innerSel = (SelectorExpr) innerCall.getFun();
                            if ("Now".equals(innerSel.getSel().getName()) && innerSel.getX() instanceof Ident) {
                                if ("time".equals(((Ident) innerSel.getX()).getName())) {
                                    // Found time.Now().Sub(t)
                                    // Change to time.Since(t)
                                    Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
                                    Ident sinceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Since", null);
                                    SelectorExpr sinceSel = new SelectorExpr(UUID.randomUUID(), c.getFun().getPrefix(), Markers.EMPTY, timeIdent, sinceIdent, null);
                                    
                                    return c.withFun(sinceSel);
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
