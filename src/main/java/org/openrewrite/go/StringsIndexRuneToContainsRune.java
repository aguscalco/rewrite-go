package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class StringsIndexRuneToContainsRune extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.ContainsRune instead of strings.IndexRune >= 0";
    }

    @Override
    public String getDescription() {
        return "Converts `strings.IndexRune(s, sub) >= 0` to `strings.ContainsRune(s, sub)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ((">=".equals(b.getOp()) || ">".equals(b.getOp()) || "==".equals(b.getOp()) || "!=".equals(b.getOp()))) {
                    
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if (isStringsIndexRuneCall(call)) {
                            boolean shouldReplace = false;
                            boolean negate = false;
                            
                            if ((">=".equals(b.getOp()) && "0".equals(lit.getValue())) || (">".equals(b.getOp()) && "-1".equals(lit.getValue())) || ("!=".equals(b.getOp()) && "-1".equals(lit.getValue()))) {
                                shouldReplace = true;
                            } else if (("==".equals(b.getOp()) && "-1".equals(lit.getValue()))) {
                                shouldReplace = true;
                                negate = true;
                            }
                            
                            if (shouldReplace) {
                                SelectorExpr sel = (SelectorExpr) call.getFun();
                                Ident containsIdent = sel.getSel().withName("ContainsRune");
                                CallExpr containsCall = call.withFun(sel.withSel(containsIdent));
                                
                                if (negate) {
                                    return new UnaryExpr(UUID.randomUUID(), b.getPrefix(), Markers.EMPTY, "!", containsCall.withPrefix(Space.EMPTY), null);
                                }
                                return containsCall.withPrefix(b.getPrefix());
                            }
                        }
                    }
                }

                return b;
            }

            private boolean isStringsIndexRuneCall(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName())) {
                        return "IndexRune".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
