package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class IndexToHasPrefix extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.HasPrefix instead of strings.Index == 0";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.Index(s, sub) == 0` to `strings.HasPrefix(s, sub)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp())) {
                    if (isStringsIndex(b.getX()) && isZero(b.getY())) {
                        return toHasPrefix((CallExpr) b.getX(), b.getPrefix());
                    } else if (isStringsIndex(b.getY()) && isZero(b.getX())) {
                        return toHasPrefix((CallExpr) b.getY(), b.getPrefix());
                    }
                }

                return b;
            }

            private boolean isStringsIndex(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName())) {
                            return "Index".equals(sel.getSel().getName());
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

            private Tree toHasPrefix(CallExpr indexCall, Space prefix) {
                SelectorExpr sel = (SelectorExpr) indexCall.getFun();
                Ident containsIdent = sel.getSel().withName("HasPrefix");
                return indexCall.withFun(sel.withSel(containsIdent)).withPrefix(prefix);
            }
        };
    }
}
