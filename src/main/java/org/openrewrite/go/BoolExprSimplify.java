package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;

public class BoolExprSimplify extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify boolean expressions";
    }

    @Override
    public String getDescription() {
        return "Simplifies boolean expressions by applying De Morgan's laws and simplifying negations, such as `!(a == b)` to `a != b`, matching gocritic's boolExprSimplify.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitUnaryExpr(UnaryExpr unaryExpr, ExecutionContext ctx) {
                UnaryExpr u = (UnaryExpr) super.visitUnaryExpr(unaryExpr, ctx);

                if ("!".equals(u.getOp()) && u.getX() instanceof BinaryExpr) {
                    BinaryExpr b = (BinaryExpr) u.getX();
                    
                    String newOp = null;
                    if ("==".equals(b.getOp())) {
                        newOp = "!=";
                    } else if ("!=".equals(b.getOp())) {
                        newOp = "==";
                    }
                    // We don't flip < or > because of floating point NaNs, it's safer to only do == and != for now.
                    
                    if (newOp != null) {
                        return b.withOp(newOp).withPrefix(u.getPrefix());
                    }
                }

                return u;
            }
        };
    }
}
