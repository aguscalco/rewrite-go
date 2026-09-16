package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class SortStringsAreSortedToSlicesIsSorted extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use slices.IsSorted instead of sort.StringsAreSorted";
    }

    @Override
    public String getDescription() {
        return "Migrates `sort.StringsAreSorted(x)` to `slices.IsSorted(x)` (Go 1.21+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "sort".equals(((Ident) sel.getX()).getName()) && "StringsAreSorted".equals(sel.getSel().getName())) {
                        
                        Ident slicesIdent = ((Ident) sel.getX()).withName("slices");
                        Ident isSortedIdent = sel.getSel().withName("IsSorted");
                        return c.withFun(sel.withX(slicesIdent).withSel(isSortedIdent));
                    }
                }

                return c;
            }
        };
    }
}
