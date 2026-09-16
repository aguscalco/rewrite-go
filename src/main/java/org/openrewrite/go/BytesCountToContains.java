package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class BytesCountToContains extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.Contains instead of bytes.Count > 0";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.Count(s, sub) > 0` to `bytes.Contains(s, sub)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if (">".equals(b.getOp())) {
                    if (isBytesCount(b.getX()) && isZero(b.getY())) {
                        return toContains((CallExpr) b.getX(), b.getPrefix(), false);
                    } else if (isBytesCount(b.getY()) && isZero(b.getX())) {
                        return toContains((CallExpr) b.getY(), b.getPrefix(), false);
                    }
                } else if ("==".equals(b.getOp())) {
                    if (isBytesCount(b.getX()) && isZero(b.getY())) {
                        return toContains((CallExpr) b.getX(), b.getPrefix(), true);
                    } else if (isBytesCount(b.getY()) && isZero(b.getX())) {
                        return toContains((CallExpr) b.getY(), b.getPrefix(), true);
                    }
                }

                return b;
            }

            private boolean isBytesCount(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName())) {
                            return "Count".equals(sel.getSel().getName());
                        }
                    }
                }
                return false;
            }

            private boolean isZero(Expr expr) {
                if (expr instanceof BasicLit) {
                    return "0".equals(((BasicLit) expr).getValue());
                }
                return false;
            }

            private Tree toContains(CallExpr countCall, Space prefix, boolean negate) {
                SelectorExpr sel = (SelectorExpr) countCall.getFun();
                Ident containsIdent = sel.getSel().withName("Contains");
                CallExpr containsCall = countCall.withFun(sel.withSel(containsIdent)).withPrefix(negate ? Space.EMPTY : prefix);
                
                if (negate) {
                    return new UnaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, "!", containsCall, null);
                }
                return containsCall;
            }
        };
    }
}
