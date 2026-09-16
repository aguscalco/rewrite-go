package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class GorillaToStdlib extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate gorilla/mux to standard library";
    }

    @Override
    public String getDescription() {
        return "Migrates `mux.NewRouter()` from `gorilla/mux` to Go 1.22+ `http.NewServeMux()`. Go 1.22's standard library router now supports method-based routing and path variables natively.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "mux".equals(((Ident) sel.getX()).getName())) {
                        if ("NewRouter".equals(sel.getSel().getName())) {
                            
                            Ident httpIdent = ((Ident) sel.getX()).withName("http");
                            Ident serveMuxIdent = sel.getSel().withName("NewServeMux");
                            return c.withFun(sel.withX(httpIdent).withSel(serveMuxIdent));
                        }
                    }
                }

                return c;
            }
        };
    }
}
