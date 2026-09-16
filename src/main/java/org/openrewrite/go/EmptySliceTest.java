package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class EmptySliceTest extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use len(s) == 0 over len(s) <= 0";
    }

    @Override
    public String getDescription() {
        return "Migrates `len(s) <= 0` to `len(s) == 0` since length cannot be negative.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("<=".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue()) && isLen(call)) {
                            return b.withOp("==");
                        }
                    } else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isLen(call)) {
                            return b.withOp("==");
                        }
                    }
                } else if ("<".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue()) && isLen(call)) {
                            return b.withOp("==");
                        }
                    }
                } else if (">".equals(b.getOp())) {
                    if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isLen(call)) {
                            return b.withOp("==");
                        }
                    }
                }

                return b;
            }

            private boolean isLen(CallExpr call) {
                if (call.getFun() instanceof Ident && "len".equals(((Ident) call.getFun()).getName())) {
                    return true;
                }
                return false;
            }
        };
    }
}
