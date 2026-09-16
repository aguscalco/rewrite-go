package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class MathExp1ToE extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.E instead of math.Exp(1)";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Exp(1)` to `math.E` for better readability and performance.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName()) && "Exp".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg;
                            if ("1".equals(lit.getValue()) || "1.0".equals(lit.getValue())) {
                                Ident mathIdent = (Ident) sel.getX();
                                Ident eIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "E", null);
                                return new SelectorExpr(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, mathIdent.withPrefix(Space.EMPTY), eIdent, null);
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
