package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

public class BytesEqualFold extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.EqualFold";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.Equal(bytes.ToUpper(a), bytes.ToUpper(b))` or `ToLower` to `bytes.EqualFold(a, b)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName()) && "Equal".equals(sel.getSel().getName())) {
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        if (isBytesToUpperOrLower(arg1) && isBytesToUpperOrLower(arg2)) {
                            CallExpr callX = (CallExpr) arg1;
                            CallExpr callY = (CallExpr) arg2;
                            
                            String methodX = ((SelectorExpr) callX.getFun()).getSel().getName();
                            String methodY = ((SelectorExpr) callY.getFun()).getSel().getName();
                            
                            if (methodX.equals(methodY)) {
                                Expr x = callX.getArgs().get(0);
                                Expr y = callY.getArgs().get(0);
                                
                                Ident equalFoldIdent = sel.getSel().withName("EqualFold");
                                return c.withFun(sel.withSel(equalFoldIdent)).withArgs(Arrays.asList(x.withPrefix(arg1.getPrefix()), y.withPrefix(arg2.getPrefix())));
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isBytesToUpperOrLower(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 1) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName())) {
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
