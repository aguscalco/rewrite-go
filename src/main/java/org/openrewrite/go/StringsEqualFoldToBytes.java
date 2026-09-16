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

public class StringsEqualFoldToBytes extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.EqualFold over strings.EqualFold for byte slices";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.EqualFold(string(a), string(b))` to `bytes.EqualFold(a, b)` for better performance.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName()) && "EqualFold".equals(sel.getSel().getName())) {
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        Expr byteSlice1 = getByteSliceCast(arg1);
                        Expr byteSlice2 = getByteSliceCast(arg2);
                        
                        if (byteSlice1 != null && byteSlice2 != null) {
                            Ident bytesIdent = new Ident(UUID.randomUUID(), sel.getX().getPrefix(), Markers.EMPTY, "bytes", null);
                            SelectorExpr newSel = sel.withX(bytesIdent);
                            
                            List<Expr> newArgs = new ArrayList<>();
                            newArgs.add(byteSlice1.withPrefix(arg1.getPrefix()));
                            newArgs.add(byteSlice2.withPrefix(arg2.getPrefix()));
                            
                            return c.withFun(newSel).withArgs(newArgs);
                        }
                    }
                }

                return c;
            }

            private Expr getByteSliceCast(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof Ident && "string".equals(((Ident) call.getFun()).getName())) {
                        if (call.getArgs() != null && call.getArgs().size() == 1) {
                            return call.getArgs().get(0);
                        }
                    }
                }
                return null;
            }
        };
    }
}
