package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class IoUtilReadDirToOsReadDir extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use os.ReadDir over ioutil.ReadDir";
    }

    @Override
    public String getDescription() {
        return "Migrates deprecated `ioutil.ReadDir` to `os.ReadDir` (Go 1.16+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "ioutil".equals(((Ident) sel.getX()).getName()) && "ReadDir".equals(sel.getSel().getName())) {
                        Ident osIdent = ((Ident) sel.getX()).withName("os");
                        return c.withFun(sel.withX(osIdent));
                    }
                }

                return c;
            }
        };
    }
}
