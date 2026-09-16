package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpRedirectConstants extends Recipe {

    private static final Map<String, String> STATUS_CODES = new HashMap<>();
    
    static {
        STATUS_CODES.put("301", "StatusMovedPermanently");
        STATUS_CODES.put("302", "StatusFound");
        STATUS_CODES.put("303", "StatusSeeOther");
        STATUS_CODES.put("307", "StatusTemporaryRedirect");
        STATUS_CODES.put("308", "StatusPermanentRedirect");
    }

    @Override
    public String getDisplayName() {
        return "Use net/http status constants for redirects";
    }

    @Override
    public String getDescription() {
        return "Migrates magic HTTP status numbers (like `302`) in `http.Redirect` to `http.StatusFound`, etc.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 4) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "http".equals(((Ident) sel.getX()).getName()) && "Redirect".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(3);
                        if (arg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg;
                            if (STATUS_CODES.containsKey(lit.getValue())) {
                                Ident httpIdent = new Ident(UUID.randomUUID(), arg.getPrefix(), Markers.EMPTY, "http", null);
                                Ident statusIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, STATUS_CODES.get(lit.getValue()), null);
                                SelectorExpr newArg = new SelectorExpr(UUID.randomUUID(), arg.getPrefix(), Markers.EMPTY, httpIdent, statusIdent, null);
                                
                                java.util.List<Expr> newArgs = new java.util.ArrayList<>(c.getArgs());
                                newArgs.set(3, newArg);
                                return c.withArgs(newArgs);
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
