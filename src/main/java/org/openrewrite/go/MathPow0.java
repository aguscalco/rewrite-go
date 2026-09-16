package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class MathPow0 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify math.Pow(x, 0)";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Pow(x, 0)` to `1`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName()) && "Pow".equals(sel.getSel().getName())) {
                        
                        Expr expArg = c.getArgs().get(1);
                        if (expArg instanceof BasicLit && "0".equals(((BasicLit) expArg).getValue())) {
                            return new BasicLit(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, "FLOAT", "1");
                        }
                    }
                }

                return c;
            }
        };
    }
}
