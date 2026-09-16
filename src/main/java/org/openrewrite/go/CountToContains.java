package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class CountToContains extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.Contains instead of strings.Count > 0";
    }

    @Override
    public String getDescription() {
        return "Converts `strings.Count(s, sub) > 0` to `strings.Contains(s, sub)` for better performance and readability.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ((">".equals(b.getOp()) || ">=".equals(b.getOp()) || "!=".equals(b.getOp()) || "==".equals(b.getOp()))) {
                    
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if (isStringsCountCall(call)) {
                            boolean shouldReplace = false;
                            boolean negate = false;
                            
                            if ((">".equals(b.getOp()) && "0".equals(lit.getValue())) || (">=".equals(b.getOp()) && "1".equals(lit.getValue())) || ("!=".equals(b.getOp()) && "0".equals(lit.getValue()))) {
                                shouldReplace = true;
                            } else if ("==".equals(b.getOp()) && "0".equals(lit.getValue())) {
                                shouldReplace = true;
                                negate = true;
                            }
                            
                            if (shouldReplace) {
                                SelectorExpr sel = (SelectorExpr) call.getFun();
                                Ident containsIdent = sel.getSel().withName("Contains");
                                CallExpr containsCall = call.withFun(sel.withSel(containsIdent));
                                
                                if (negate) {
                                    return new org.openrewrite.go.tree.UnaryExpr(java.util.UUID.randomUUID(), b.getPrefix(), org.openrewrite.marker.Markers.EMPTY, "!", containsCall.withPrefix(org.openrewrite.go.tree.Space.EMPTY), null);
                                }
                                return containsCall.withPrefix(b.getPrefix());
                            }
                        }
                    }
                }

                return b;
            }

            private boolean isStringsCountCall(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName())) {
                        return "Count".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
