package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class StringsReplaceZero extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify strings.Replace with n=0";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.Replace(s, old, new, 0)` to `s`.";
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
                        if (nArg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) nArg;
                            if ("0".equals(lit.getValue())) {
                                return c.getArgs().get(0).withPrefix(c.getPrefix());
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
