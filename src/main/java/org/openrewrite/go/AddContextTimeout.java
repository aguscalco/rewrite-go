package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

/**
 * Adds timeout to context.Background() or context.TODO() calls.
 *
 * This recipe identifies calls to context.Background() or context.TODO() and
 * wraps them with context.WithTimeout() to prevent operations from hanging indefinitely.
 *
 * Example:
 *   ctx := context.Background()
 *   becomes:
 *   ctx, cancel := context.WithTimeout(context.Background(), timeout)
 *   defer cancel()
 *
 * Options:
 * - timeoutDuration: The timeout duration to use (default: "30 * time.Second")
 * - variableName: The context variable name to look for (default: "ctx")
 */
public class AddContextTimeout extends Recipe {

    @Option(displayName = "Timeout duration",
            description = "The timeout duration to use in context.WithTimeout",
            example = "30 * time.Second")
    String timeoutDuration = "30 * time.Second";

    @Option(displayName = "Context variable name",
            description = "The context variable name to look for",
            example = "ctx")
    String variableName = "ctx";

    @Override
    public String getDisplayName() {
        return "Add timeout to context";
    }

    @Override
    public String getDescription() {
        return "Wrap context.Background() or context.TODO() with context.WithTimeout to prevent hanging.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = (AssignStmt) super.visitAssignStmt(assignStmt, ctx);
                
                if (!isContextAssignment(a)) {
                    return a;
                }
                
                CallExpr originalCall = (CallExpr) a.getRhs().get(0);
                
                Ident contextIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    originalCall.getMarkers(),
                    "context",
                    null
                );
                
                Ident withTimeoutIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    originalCall.getMarkers(),
                    "WithTimeout",
                    null
                );
                
                SelectorExpr withTimeoutSelector = new SelectorExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    originalCall.getMarkers(),
                    contextIdent,
                    withTimeoutIdent,
                    null
                );
                
                Ident timeoutExpr = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    originalCall.getMarkers(),
                    timeoutDuration,
                    null
                );
                
                CallExpr withTimeoutCall = new CallExpr(
                    Tree.randomId(),
                    originalCall.getPrefix(),
                    originalCall.getMarkers(),
                    withTimeoutSelector,
                    java.util.Arrays.asList(originalCall, timeoutExpr),
                    false,
                    null
                );
                
                Ident cancelIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    originalCall.getMarkers(),
                    "cancel",
                    null
                );
                
                AssignStmt newAssign = new AssignStmt(
                    a.getId(),
                    a.getPrefix(),
                    a.getMarkers(),
                    java.util.Arrays.asList(a.getLhs().get(0), cancelIdent),
                    ":=",
                    java.util.Collections.singletonList(withTimeoutCall)
                );
                
                return newAssign;
            }
            
            private boolean isContextAssignment(AssignStmt assign) {
                if (assign.getLhs().size() != 1 || assign.getRhs().size() != 1) {
                    return false;
                }
                
                if (!(assign.getLhs().get(0) instanceof Ident)) {
                    return false;
                }
                
                Ident lhs = (Ident) assign.getLhs().get(0);
                if (!variableName.equals(lhs.getName())) {
                    return false;
                }
                
                if (!(assign.getRhs().get(0) instanceof CallExpr)) {
                    return false;
                }
                
                CallExpr call = (CallExpr) assign.getRhs().get(0);
                if (!(call.getFun() instanceof SelectorExpr)) {
                    return false;
                }
                
                SelectorExpr sel = (SelectorExpr) call.getFun();
                if (!(sel.getX() instanceof Ident)) {
                    return false;
                }
                
                Ident pkg = (Ident) sel.getX();
                Ident method = sel.getSel();
                
                return "context".equals(pkg.getName()) && 
                       ("Background".equals(method.getName()) || "TODO".equals(method.getName()));
            }
        };
    }
}
