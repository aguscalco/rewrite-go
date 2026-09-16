package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class BytesCompareNotEqual extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use !bytes.Equal instead of bytes.Compare != 0";
    }

    @Override
    public String getDescription() {
        return "Replaces `bytes.Compare(a, b) != 0` with `!bytes.Equal(a, b)` for better performance and readability.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("!=".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue()) && isBytesCompare(call)) {
                            return toNotEqual(call, b.getPrefix());
                        }
                    } else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isBytesCompare(call)) {
                            return toNotEqual(call, b.getPrefix());
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
            
            private Tree toNotEqual(CallExpr call, Space prefix) {
                SelectorExpr sel = (SelectorExpr) call.getFun();
                Ident equalIdent = sel.getSel().withName("Equal");
                CallExpr equalCall = call.withFun(sel.withSel(equalIdent)).withPrefix(Space.EMPTY);
                return new UnaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, "!", equalCall, null);
            }
        };
    }
}
