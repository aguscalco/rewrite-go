package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class UseMinMaxBuiltins extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use min/max builtins";
    }

    @Override
    public String getDescription() {
        return "Migrates math.Min and math.Max to the Go 1.21+ min and max built-in functions.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                        String methodName = sel.getSel().getName();
                        if ("Min".equals(methodName) || "Max".equals(methodName)) {
                            Ident builtin = new Ident(
                                UUID.randomUUID(),
                                sel.getPrefix(),
                                Markers.EMPTY,
                                methodName.toLowerCase(),
                                null
                            );
                            return c.withFun(builtin);
                        }
                    }
                }
                return c;
            }
        };
    }
}
