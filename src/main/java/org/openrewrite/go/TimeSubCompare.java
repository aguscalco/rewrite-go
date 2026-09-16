package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class TimeSubCompare extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use time comparison methods instead of Sub()";
    }

    @Override
    public String getDescription() {
        return "Migrates `t1.Sub(t2) > 0` to `t1.After(t2)`, `t1.Sub(t2) < 0` to `t1.Before(t2)`, and `t1.Sub(t2) == 0` to `t1.Equal(t2)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                    CallExpr call = (CallExpr) b.getX();
                    BasicLit lit = (BasicLit) b.getY();
                    
                    if ("0".equals(lit.getValue()) && isTimeSub(call)) {
                        String newMethod = null;
                        if (">".equals(b.getOp())) {
                            newMethod = "After";
                        } else if ("<".equals(b.getOp())) {
                            newMethod = "Before";
                        } else if ("==".equals(b.getOp())) {
                            newMethod = "Equal";
                        }
                        
                        if (newMethod != null) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            Ident newIdent = sel.getSel().withName(newMethod);
                            return call.withFun(sel.withSel(newIdent)).withPrefix(b.getPrefix());
                        }
                    }
                }

                return b;
            }

            private boolean isTimeSub(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    return "Sub".equals(sel.getSel().getName());
                }
                return false;
            }
        };
    }
}
