package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class StringsCompareNotEqual extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use a != b instead of strings.Compare != 0";
    }

    @Override
    public String getDescription() {
        return "Replaces `strings.Compare(a, b) != 0` with `a != b` for better performance and readability.";
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
                        
                        if ("0".equals(lit.getValue()) && isStringsCompare(call)) {
                            return toNotEqual(call, b.getPrefix());
                        }
                    } else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isStringsCompare(call)) {
                            return toNotEqual(call, b.getPrefix());
                        }
                    }
                }

                return b;
            }

            private boolean isStringsCompare(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName())) {
                        return "Compare".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
            
            private Tree toNotEqual(CallExpr call, Space prefix) {
                Expr arg1 = call.getArgs().get(0).withPrefix(Space.EMPTY);
                Expr arg2 = call.getArgs().get(1).withPrefix(Space.build(" "));
                
                return new BinaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, arg1, "!=", arg2, null);
            }
        };
    }
}
