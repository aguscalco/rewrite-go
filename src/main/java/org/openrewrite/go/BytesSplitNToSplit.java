package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

public class BytesSplitNToSplit extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.Split instead of bytes.SplitN with n=-1";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.SplitN(s, sep, -1)` to `bytes.Split(s, sep)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 3) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName()) && "SplitN".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(2);
                        if (arg instanceof UnaryExpr) {
                            UnaryExpr unary = (UnaryExpr) arg;
                            if ("-".equals(unary.getOp()) && unary.getX() instanceof BasicLit) {
                                String val = ((BasicLit) unary.getX()).getValue();
                                if ("1".equals(val)) {
                                    Ident splitIdent = sel.getSel().withName("Split");
                                    return c.withFun(sel.withSel(splitIdent)).withArgs(Arrays.asList(c.getArgs().get(0), c.getArgs().get(1)));
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
