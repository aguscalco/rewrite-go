package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class GinBindValidation extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use ShouldBind over Bind in Gin";
    }

    @Override
    public String getDescription() {
        return "Migrates `c.Bind()` to `c.ShouldBind()` in the Gin web framework to prevent automatic 400 response writing, giving developers explicit control over error handling.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    
                    // Gin context is usually named 'c' or 'ctx'
                    if (sel.getX() instanceof Ident) {
                        String receiverName = ((Ident) sel.getX()).getName();
                        if ("c".equals(receiverName) || "ctx".equals(receiverName)) {
                            String method = sel.getSel().getName();
                            
                            String newMethod = null;
                            if ("Bind".equals(method)) {
                                newMethod = "ShouldBind";
                            } else if ("BindJSON".equals(method)) {
                                newMethod = "ShouldBindJSON";
                            } else if ("BindXML".equals(method)) {
                                newMethod = "ShouldBindXML";
                            } else if ("BindQuery".equals(method)) {
                                newMethod = "ShouldBindQuery";
                            } else if ("BindYAML".equals(method)) {
                                newMethod = "ShouldBindYAML";
                            } else if ("BindHeader".equals(method)) {
                                newMethod = "ShouldBindHeader";
                            } else if ("BindUri".equals(method)) {
                                newMethod = "ShouldBindUri";
                            }
                            
                            if (newMethod != null) {
                                return c.withFun(sel.withSel(sel.getSel().withName(newMethod)));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
