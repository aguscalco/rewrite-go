package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

/**
 * Replaces context.TODO() calls with the ctx parameter from the enclosing function.
 *
 * This recipe looks for context.TODO() calls and replaces them with the context
 * parameter (ctx) from the enclosing function signature. This is a common pattern
 * when adding context propagation to existing code.
 *
 * Example:
 *   func process(ctx context.Context) {
 *     doSomething(context.TODO())  // becomes: doSomething(ctx)
 *   }
 */
public class ReplaceContextTODO extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace context.TODO() with ctx";
    }

    @Override
    public String getDescription() {
        return "Replace context.TODO() calls with the ctx parameter from the enclosing function.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                if (!isContextTODO(c)) {
                    return c;
                }
                
                Ident ctxIdent = new Ident(
                    Tree.randomId(),
                    c.getPrefix(),
                    c.getMarkers(),
                    "ctx",
                    null
                );
                
                return ctxIdent;
            }
            
            private boolean isContextTODO(CallExpr call) {
                if (!(call.getFun() instanceof SelectorExpr)) {
                    return false;
                }
                
                SelectorExpr sel = (SelectorExpr) call.getFun();
                if (!(sel.getX() instanceof Ident)) {
                    return false;
                }
                
                Ident pkg = (Ident) sel.getX();
                Ident method = sel.getSel();
                
                return "context".equals(pkg.getName()) && "TODO".equals(method.getName());
            }
        };
    }
}
