package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class AtomicPointerMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate atomic.Value to type-safe atomic.Pointer";
    }

    @Override
    public String getDescription() {
        return "Flags usage of the weakly-typed atomic.Value and recommends migrating to the type-safe atomic.Pointer[T] introduced in Go 1.19.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public SelectorExpr visitSelectorExpr(SelectorExpr selectorExpr, ExecutionContext ctx) {
                SelectorExpr sel = (SelectorExpr) super.visitSelectorExpr(selectorExpr, ctx);
                
                if (sel.getX() instanceof Ident && "atomic".equals(((Ident) sel.getX()).getName())) {
                    if ("Value".equals(sel.getSel().getName())) {
                        return SearchResult.found(sel, "Go 1.19+: Consider replacing atomic.Value with the type-safe atomic.Pointer[T].");
                    }
                }
                
                return sel;
            }
        };
    }
}
