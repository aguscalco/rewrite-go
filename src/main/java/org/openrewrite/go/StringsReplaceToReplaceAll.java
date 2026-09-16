package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class StringsReplaceToReplaceAll extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.ReplaceAll";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.Replace(s, old, new, -1)` to `strings.ReplaceAll(s, old, new)` (Go 1.12+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 4) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName()) && "Replace".equals(sel.getSel().getName())) {
                        
                        Expr nArg = c.getArgs().get(3);
                        if (isMinusOne(nArg)) {
                            Ident replaceAllIdent = sel.getSel().withName("ReplaceAll");
                            return c.withFun(sel.withSel(replaceAllIdent)).withArgs(c.getArgs().subList(0, 3));
                        }
                    }
                }

                return c;
            }

            private boolean isMinusOne(Expr expr) {
                if (expr instanceof UnaryExpr) {
                    UnaryExpr u = (UnaryExpr) expr;
                    if ("-".equals(u.getOp()) && u.getX() instanceof BasicLit) {
                        return "1".equals(((BasicLit) u.getX()).getValue());
                    }
                }
                return false;
            }
        };
    }
}
