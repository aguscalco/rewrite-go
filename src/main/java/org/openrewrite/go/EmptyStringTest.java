package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class EmptyStringTest extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify empty string checks";
    }

    @Override
    public String getDescription() {
        return "Converts `len(s) == 0` to `s == \"\"` for better readability, matching gocritic's emptyStringTest check.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public BinaryExpr visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp()) || "!=".equals(b.getOp())) {
                    boolean isLenEqZero = false;
                    Expr sArg = null;
                    
                    // Check if LHS is len(s) and RHS is 0
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        if (isLenCall(call) && "0".equals(lit.getValue())) {
                            isLenEqZero = true;
                            sArg = call.getArgs().get(0);
                        }
                    }
                    // Check if RHS is len(s) and LHS is 0
                    else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        if (isLenCall(call) && "0".equals(lit.getValue())) {
                            isLenEqZero = true;
                            sArg = call.getArgs().get(0);
                        }
                    }

                    if (isLenEqZero && sArg != null) {
                        BasicLit emptyStr = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "STRING", "\"\"");
                        return b.withX(sArg.withPrefix(b.getX().getPrefix())).withY(emptyStr);
                    }
                }

                return b;
            }

            private boolean isLenCall(CallExpr call) {
                if (call.getFun() instanceof Ident && "len".equals(((Ident) call.getFun()).getName())) {
                    return call.getArgs() != null && call.getArgs().size() == 1;
                }
                return false;
            }
        };
    }
}
