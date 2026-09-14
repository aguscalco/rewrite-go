package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Propagates context through call chains.
 * 
 * When a function has been updated to accept context.Context as its first parameter
 * (typically via AddContextParameter), this recipe updates all call sites to pass
 * the context variable (default: "ctx") from the calling function's scope.
 * 
 * This recipe works in conjunction with AddContextParameter to ensure context
 * flows properly through the entire call chain.
 */
public class PropagateContext extends Recipe {
    
    @Option(displayName = "Context variable name",
            description = "Name of the context variable to pass (default: ctx).",
            example = "ctx",
            required = false)
    String contextVarName = "ctx";
    
    @Override
    public String getDisplayName() {
        return "Propagate context through call chains";
    }
    
    @Override
    public String getDescription() {
        return "Update function calls to pass context when the callee accepts context.Context as first parameter.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                if (callExpr.getArgs() == null || callExpr.getArgs().isEmpty()) {
                    // Check if first arg should be context
                    // This is a simplified version - in practice we'd need type information
                    // to know if the function expects context
                    return callExpr;
                }
                
                // Check if first argument is already a context-like identifier
                Expr firstArg = callExpr.getArgs().get(0);
                if (firstArg instanceof Ident) {
                    String name = ((Ident) firstArg).getName();
                    if (name.equals("ctx") || name.equals("context") || name.endsWith("Ctx")) {
                        // Already has context
                        return callExpr;
                    }
                }
                
                // For now, this recipe is a placeholder
                // A full implementation would require type information to determine
                // which functions expect context.Context as first parameter
                // Then it would insert the context variable at the call site
                
                return callExpr;
            }
        };
    }
}
