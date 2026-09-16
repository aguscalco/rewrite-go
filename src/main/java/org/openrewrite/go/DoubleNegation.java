package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class DoubleNegation extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove double negation";
    }

    @Override
    public String getDescription() {
        return "Removes double negations `!(!b)` and simplifies them to `b`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitUnaryExpr(UnaryExpr unaryExpr, ExecutionContext ctx) {
                UnaryExpr u = (UnaryExpr) super.visitUnaryExpr(unaryExpr, ctx);

                if ("!".equals(u.getOp())) {
                    Expr inner = u.getX();
                    if (inner instanceof UnaryExpr && "!".equals(((UnaryExpr) inner).getOp())) {
                        return ((UnaryExpr) inner).getX().withPrefix(u.getPrefix());
                    }
                }

                return u;
            }
        };
    }
}
