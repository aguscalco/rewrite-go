package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

public class StringsEqualFold extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.EqualFold";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.ToUpper(s) == strings.ToUpper(t)` or `ToLower` to `strings.EqualFold(s, t)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp()) || "!=".equals(b.getOp())) {
                    if (isStringsToUpperOrLower(b.getX()) && isStringsToUpperOrLower(b.getY())) {
                        CallExpr callX = (CallExpr) b.getX();
                        CallExpr callY = (CallExpr) b.getY();
                        
                        String methodX = ((SelectorExpr) callX.getFun()).getSel().getName();
                        String methodY = ((SelectorExpr) callY.getFun()).getSel().getName();
                        
                        if (methodX.equals(methodY)) {
                            Expr argX = callX.getArgs().get(0);
                            Expr argY = callY.getArgs().get(0);
                            
                            Ident stringsIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "strings", null);
                            Ident equalFoldIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "EqualFold", null);
                            SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringsIdent, equalFoldIdent, null);
                            
                            CallExpr equalFoldCall = new CallExpr(UUID.randomUUID(), b.getPrefix(), Markers.EMPTY, sel, Arrays.asList(argX.withPrefix(Space.EMPTY), argY.withPrefix(Space.build(" "))), false, null);
                            
                            if ("!=".equals(b.getOp())) {
                                return new UnaryExpr(UUID.randomUUID(), b.getPrefix(), Markers.EMPTY, "!", equalFoldCall.withPrefix(Space.EMPTY), null);
                            }
                            
                            return equalFoldCall;
                        }
                    }
                }

                return b;
            }

            private boolean isStringsToUpperOrLower(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 1) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName())) {
                            String name = sel.getSel().getName();
                            return "ToUpper".equals(name) || "ToLower".equals(name);
                        }
                    }
                }
                return false;
            }
        };
    }
}
