package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;

public class Underef extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove redundant deref-address-of pairs";
    }

    @Override
    public String getDescription() {
        return "Removes redundant `*&` pairs, e.g. `*&x` becomes `x`, matching gocritic's underef.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitStarExpr(StarExpr starExpr, ExecutionContext ctx) {
                StarExpr s = (StarExpr) super.visitStarExpr(starExpr, ctx);

                if (s.getX() instanceof UnaryExpr) {
                    UnaryExpr u = (UnaryExpr) s.getX();
                    if ("&".equals(u.getOp())) {
                        return u.getX().withPrefix(s.getPrefix());
                    }
                }

                return s;
            }
        };
    }
}
