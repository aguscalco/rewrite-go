package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class BytesCompareToEqual extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.Equal instead of bytes.Compare == 0";
    }

    @Override
    public String getDescription() {
        return "Replaces `bytes.Compare(a, b) == 0` with `bytes.Equal(a, b)` for better performance and readability.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue()) && isBytesCompare(call)) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            Ident equalIdent = sel.getSel().withName("Equal");
                            return call.withFun(sel.withSel(equalIdent)).withPrefix(b.getPrefix());
                        }
                    } else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isBytesCompare(call)) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            Ident equalIdent = sel.getSel().withName("Equal");
                            return call.withFun(sel.withSel(equalIdent)).withPrefix(b.getPrefix());
                        }
                    }
                }

                return b;
            }

            private boolean isBytesCompare(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName())) {
                        return "Compare".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
