package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class TimeUntilNow extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify time.Until(time.Now())";
    }

    @Override
    public String getDescription() {
        return "Migrates `time.Until(time.Now())` to `0`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "time".equals(((Ident) sel.getX()).getName()) && "Until".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof CallExpr) {
                            CallExpr argCall = (CallExpr) arg;
                            if (argCall.getFun() instanceof SelectorExpr && (argCall.getArgs() == null || argCall.getArgs().isEmpty())) {
                                SelectorExpr argSel = (SelectorExpr) argCall.getFun();
                                if (argSel.getX() instanceof Ident && "time".equals(((Ident) argSel.getX()).getName()) && "Now".equals(argSel.getSel().getName())) {
                                    return new BasicLit(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, "INT", "0");
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
