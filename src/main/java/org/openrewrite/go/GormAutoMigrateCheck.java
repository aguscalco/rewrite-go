package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class GormAutoMigrateCheck extends Recipe {

    @Override
    public String getDisplayName() {
        return "Warn against GORM AutoMigrate in production";
    }

    @Override
    public String getDescription() {
        return "Flags `db.AutoMigrate(...)` usage, which is unsafe for production schema management. Recommends using migration tools like `golang-migrate` instead.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && ("db".equals(((Ident) sel.getX()).getName()) || "gormDb".equals(((Ident) sel.getX()).getName()))) {
                        if ("AutoMigrate".equals(sel.getSel().getName())) {
                            return SearchResult.found(c, "GORM Warning: AutoMigrate is unsafe for production. Use proper schema migration tools (e.g. golang-migrate, goose) for predictable, versioned schema changes.");
                        }
                    }
                }

                return c;
            }
        };
    }
}
