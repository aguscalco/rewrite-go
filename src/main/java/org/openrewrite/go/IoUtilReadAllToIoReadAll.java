package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class IoUtilReadAllToIoReadAll extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use io.ReadAll over ioutil.ReadAll";
    }

    @Override
    public String getDescription() {
        return "Migrates deprecated `ioutil.ReadAll` to `io.ReadAll` (Go 1.16+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "ioutil".equals(((Ident) sel.getX()).getName()) && "ReadAll".equals(sel.getSel().getName())) {
                        Ident ioIdent = ((Ident) sel.getX()).withName("io");
                        return c.withFun(sel.withX(ioIdent));
                    }
                }

                return c;
            }
        };
    }
}
