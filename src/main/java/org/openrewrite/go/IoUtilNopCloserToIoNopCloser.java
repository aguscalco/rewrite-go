package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class IoUtilNopCloserToIoNopCloser extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use io.NopCloser over ioutil.NopCloser";
    }

    @Override
    public String getDescription() {
        return "Migrates deprecated `ioutil.NopCloser` to `io.NopCloser` (Go 1.16+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "ioutil".equals(((Ident) sel.getX()).getName()) && "NopCloser".equals(sel.getSel().getName())) {
                        Ident ioIdent = ((Ident) sel.getX()).withName("io");
                        return c.withFun(sel.withX(ioIdent));
                    }
                }

                return c;
            }
        };
    }
}
