package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BytesEqualFoldToStrings extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.EqualFold over bytes.EqualFold for strings";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.EqualFold([]byte(a), []byte(b))` to `strings.EqualFold(a, b)` for better performance.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName()) && "EqualFold".equals(sel.getSel().getName())) {
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        Expr stringVar1 = getStringVar(arg1);
                        Expr stringVar2 = getStringVar(arg2);
                        
                        if (stringVar1 != null && stringVar2 != null) {
                            Ident stringsIdent = new Ident(UUID.randomUUID(), sel.getX().getPrefix(), Markers.EMPTY, "strings", null);
                            SelectorExpr newSel = sel.withX(stringsIdent);
                            
                            List<Expr> newArgs = new ArrayList<>();
                            newArgs.add(stringVar1.withPrefix(arg1.getPrefix()));
                            newArgs.add(stringVar2.withPrefix(arg2.getPrefix()));
                            
                            return c.withFun(newSel).withArgs(newArgs);
                        }
                    }
                }

                return c;
            }

            private Expr getStringVar(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof ArrayTypeExpr) {
                        ArrayTypeExpr arr = (ArrayTypeExpr) call.getFun();
                        if (arr.getElt() instanceof Ident && "byte".equals(((Ident) arr.getElt()).getName()) && arr.getLen() == null) {
                            if (call.getArgs() != null && call.getArgs().size() == 1) {
                                return call.getArgs().get(0);
                            }
                        }
                    }
                }
                return null;
            }
        };
    }
}
