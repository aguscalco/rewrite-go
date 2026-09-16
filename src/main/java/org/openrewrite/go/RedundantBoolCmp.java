package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class RedundantBoolCmp extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove redundant boolean comparisons";
    }

    @Override
    public String getDescription() {
        return "Migrates `b == true` to `b` and `b == false` to `!b`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp()) || "!=".equals(b.getOp())) {
                    if (isBoolLit(b.getY())) {
                        return simplify(b.getX(), (Ident) b.getY(), b.getOp(), b.getPrefix());
                    } else if (isBoolLit(b.getX())) {
                        return simplify(b.getY(), (Ident) b.getX(), b.getOp(), b.getPrefix());
                    }
                }

                return b;
            }

            private boolean isBoolLit(Expr expr) {
                if (expr instanceof Ident) {
                    String name = ((Ident) expr).getName();
                    return "true".equals(name) || "false".equals(name);
                }
                return false;
            }
            
            private Tree simplify(Expr other, Ident boolLit, String op, Space prefix) {
                boolean isTrue = "true".equals(boolLit.getName());
                boolean isEq = "==".equals(op);
                
                boolean wantNegation = false;
                if (isEq && !isTrue) {
                    wantNegation = true;
                } else if (!isEq && isTrue) {
                    wantNegation = true;
                }
                
                if (wantNegation) {
                    return new UnaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, "!", other.withPrefix(Space.EMPTY), null);
                } else {
                    return other.withPrefix(prefix);
                }
            }
        };
    }
}
