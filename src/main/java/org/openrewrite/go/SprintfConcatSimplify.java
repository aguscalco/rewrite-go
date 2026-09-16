package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class SprintfConcatSimplify extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify string concatenation";
    }

    @Override
    public String getDescription() {
        return "Replaces `fmt.Sprintf(\"%s%s\", a, b)` with `a + b` for better readability and performance.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName()) && "Sprintf".equals(sel.getSel().getName())) {
                        if (c.getArgs() != null && c.getArgs().size() == 3) {
                            Expr formatArg = c.getArgs().get(0);
                            
                            if (formatArg instanceof BasicLit) {
                                BasicLit formatLit = (BasicLit) formatArg;
                                if ("\"%s%s\"".equals(formatLit.getValue()) || "`%s%s`".equals(formatLit.getValue())) {
                                    Expr arg1 = c.getArgs().get(1);
                                    Expr arg2 = c.getArgs().get(2);
                                    
                                    return new BinaryExpr(UUID.randomUUID(), c.getPrefix(), Markers.EMPTY, arg1.withPrefix(Space.EMPTY), "+", arg2.withPrefix(Space.build(" ")), null);
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
