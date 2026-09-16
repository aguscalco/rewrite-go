package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class TimeSleepZero extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use runtime.Gosched() over time.Sleep(0)";
    }

    @Override
    public String getDescription() {
        return "Migrates `time.Sleep(0)` to `runtime.Gosched()` to explicitly indicate yielding the processor.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "time".equals(((Ident) sel.getX()).getName()) && "Sleep".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BasicLit && "0".equals(((BasicLit) arg).getValue())) {
                            Ident runtimeIdent = new Ident(UUID.randomUUID(), sel.getX().getPrefix(), Markers.EMPTY, "runtime", null);
                            Ident goschedIdent = sel.getSel().withName("Gosched");
                            SelectorExpr newSel = sel.withX(runtimeIdent).withSel(goschedIdent);
                            return c.withFun(newSel).withArgs(Collections.emptyList());
                        }
                    }
                }

                return c;
            }
        };
    }
}
