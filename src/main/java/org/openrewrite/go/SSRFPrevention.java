package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class SSRFPrevention extends Recipe {

    @Override
    public String getDisplayName() {
        return "Detect potential Server-Side Request Forgery (SSRF)";
    }

    @Override
    public String getDescription() {
        return "Flags `http.Get`, `http.Post`, and similar HTTP client methods that accept dynamic variables for URLs, which can lead to SSRF vulnerabilities if the URL is user-controlled.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "http".equals(((Ident) sel.getX()).getName())) {
                        String method = sel.getSel().getName();
                        
                        if (("Get".equals(method) || "Post".equals(method) || "Head".equals(method)) && c.getArgs() != null && !c.getArgs().isEmpty()) {
                            Expr urlArg = c.getArgs().get(0);
                            
                            // If the URL is not a hardcoded string literal, it's dynamic and potentially user-controlled.
                            if (!(urlArg instanceof BasicLit) && !(urlArg instanceof CallExpr && isUrlParse((CallExpr)urlArg))) {
                                return SearchResult.found(c, "SSRF Warning: Ensure this dynamic URL is strictly validated against an allowlist before making an HTTP request.");
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isUrlParse(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "url".equals(((Ident) sel.getX()).getName())) {
                        return "Parse".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
