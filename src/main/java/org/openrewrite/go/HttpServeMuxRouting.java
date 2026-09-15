package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class HttpServeMuxRouting extends Recipe {

    @Override
    public String getDisplayName() {
        return "Adopt Go 1.22 ServeMux routing patterns";
    }

    @Override
    public String getDescription() {
        return "Flags http.HandleFunc and ServeMux.HandleFunc calls lacking HTTP verbs (e.g. \"GET /path\") for migration to Go 1.22 routing features.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    String method = sel.getSel().getName();
                    
                    if (("HandleFunc".equals(method) || "Handle".equals(method)) && 
                        c.getArgs() != null && !c.getArgs().isEmpty()) {
                        
                        Expr firstArg = c.getArgs().get(0);
                        if (firstArg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) firstArg;
                            if ("STRING".equalsIgnoreCase(lit.getKind()) || lit.getValue().startsWith("\"") || lit.getValue().startsWith("`")) {
                                String val = lit.getValue();
                                // if the path doesn't have a space, it doesn't specify an HTTP verb
                                if (val != null && val.length() >= 2 && !val.contains(" ")) {
                                    return SearchResult.found(c, "Go 1.22+: Consider adding an HTTP verb to this route (e.g. \"GET " + val.substring(1) + "\")");
                                }
                            }
                        }
                    }
                }
                
                return c;
            }
        };
    }
}
