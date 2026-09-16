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

public class HttpStatusConstants extends Recipe {

    private static final Map<String, String> STATUS_CODES = new HashMap<>();
    
    static {
        STATUS_CODES.put("200", "StatusOK");
        STATUS_CODES.put("201", "StatusCreated");
        STATUS_CODES.put("202", "StatusAccepted");
        STATUS_CODES.put("204", "StatusNoContent");
        STATUS_CODES.put("301", "StatusMovedPermanently");
        STATUS_CODES.put("302", "StatusFound");
        STATUS_CODES.put("304", "StatusNotModified");
        STATUS_CODES.put("400", "StatusBadRequest");
        STATUS_CODES.put("401", "StatusUnauthorized");
        STATUS_CODES.put("403", "StatusForbidden");
        STATUS_CODES.put("404", "StatusNotFound");
        STATUS_CODES.put("405", "StatusMethodNotAllowed");
        STATUS_CODES.put("409", "StatusConflict");
        STATUS_CODES.put("500", "StatusInternalServerError");
        STATUS_CODES.put("502", "StatusBadGateway");
        STATUS_CODES.put("503", "StatusServiceUnavailable");
        STATUS_CODES.put("504", "StatusGatewayTimeout");
    }

    @Override
    public String getDisplayName() {
        return "Use net/http status constants";
    }

    @Override
    public String getDescription() {
        return "Migrates magic HTTP status numbers (like `200` or `404`) in `w.WriteHeader` to `http.StatusOK`, `http.StatusNotFound`, etc.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if ("WriteHeader".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg;
                            if (STATUS_CODES.containsKey(lit.getValue())) {
                                Ident httpIdent = new Ident(UUID.randomUUID(), arg.getPrefix(), Markers.EMPTY, "http", null);
                                Ident statusIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, STATUS_CODES.get(lit.getValue()), null);
                                SelectorExpr newArg = new SelectorExpr(UUID.randomUUID(), arg.getPrefix(), Markers.EMPTY, httpIdent, statusIdent, null);
                                return c.withArgs(java.util.Collections.singletonList(newArg));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
