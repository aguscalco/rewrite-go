package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class SortFloat64sToSlicesSort extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use slices.Sort instead of sort.Float64s";
    }

    @Override
    public String getDescription() {
        return "Migrates `sort.Float64s(x)` to `slices.Sort(x)` (Go 1.21+).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "sort".equals(((Ident) sel.getX()).getName()) && "Float64s".equals(sel.getSel().getName())) {
                        
                        Ident slicesIdent = ((Ident) sel.getX()).withName("slices");
                        Ident sortIdent = sel.getSel().withName("Sort");
                        return c.withFun(sel.withX(slicesIdent).withSel(sortIdent));
                    }
                }

                return c;
            }
        };
    }
}
