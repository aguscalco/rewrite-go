package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class YodaCondition extends Recipe {

    @Override
    public String getDisplayName() {
        return "Fix Yoda conditions";
    }

    @Override
    public String getDescription() {
        return "Reverses Yoda-style conditions (e.g. `42 == x` to `x == 42`) for better readability.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public BinaryExpr visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if (isConstant(b.getX()) && !isConstant(b.getY())) {
                    String newOp = swapOp(b.getOp());
                    if (newOp != null) {
                        Expr newX = b.getY().withPrefix(b.getX().getPrefix());
                        Expr newY = b.getX().withPrefix(Space.build(" "));
                        
                        return b.withX(newX).withY(newY).withOp(newOp);
                    }
                }

                return b;
            }

            private boolean isConstant(Expr e) {
                if (e instanceof BasicLit) {
                    return true;
                }
                if (e instanceof Ident && "nil".equals(((Ident) e).getName())) {
                    return true;
                }
                return false;
            }

            private String swapOp(String op) {
                switch (op) {
                    case "==": return "==";
                    case "!=": return "!=";
                    case "<": return ">";
                    case ">": return "<";
                    case "<=": return ">=";
                    case ">=": return "<=";
                    default: return null; // Not a comparison operator we can safely swap
                }
            }
        };
    }
}
