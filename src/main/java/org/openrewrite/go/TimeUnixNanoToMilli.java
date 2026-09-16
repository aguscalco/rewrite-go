package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class TimeUnixNanoToMilli extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use time.UnixMilli";
    }

    @Override
    public String getDescription() {
        return "Migrates `time.Now().UnixNano() / 1000000` to `time.Now().UnixMilli()` (Go 1.17+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("/".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("1000000".equals(lit.getValue()) || "1e6".equals(lit.getValue())) {
                            if (isUnixNano(call)) {
                                SelectorExpr sel = (SelectorExpr) call.getFun();
                                Ident milliIdent = sel.getSel().withName("UnixMilli");
                                return call.withFun(sel.withSel(milliIdent)).withPrefix(b.getPrefix());
                            }
                        }
                    }
                }

                return b;
            }

            private boolean isUnixNano(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr && (call.getArgs() == null || call.getArgs().isEmpty())) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if ("UnixNano".equals(sel.getSel().getName())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
