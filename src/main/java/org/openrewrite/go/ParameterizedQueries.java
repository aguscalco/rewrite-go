package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Prevents SQL injection by migrating fmt.Sprintf format strings in SQL execution methods
 * to parameterized queries.
 *
 * Example:
 *   db.Query(fmt.Sprintf("SELECT * FROM users WHERE name = '%s'", name))
 *   // becomes
 *   db.Query("SELECT * FROM users WHERE name = ?", name)
 */
public class ParameterizedQueries extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use parameterized queries";
    }

    @Override
    public String getDescription() {
        return "Convert string formatting in SQL queries to parameterized queries to prevent SQL injection.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                // Look for db.Query, db.QueryRow, db.Exec
                if (!(c.getFun() instanceof SelectorExpr)) {
                    return c;
                }
                
                SelectorExpr sel = (SelectorExpr) c.getFun();
                String methodName = sel.getSel().getName();
                if (!"Query".equals(methodName) && !"QueryRow".equals(methodName) && !"Exec".equals(methodName)) {
                    return c;
                }
                
                if (c.getArgs().isEmpty()) {
                    return c;
                }
                
                // First argument might be context.Context, check if first arg is fmt.Sprintf or if second arg is
                // For simplicity, let's look for fmt.Sprintf anywhere in the arguments that could be a query string.
                // Usually the query is the first arg, or second if the first is context.Context.
                int queryArgIdx = -1;
                CallExpr sprintfCall = null;
                
                for (int i = 0; i < c.getArgs().size(); i++) {
                    Expr arg = c.getArgs().get(i);
                    if (isFmtSprintf(arg)) {
                        queryArgIdx = i;
                        sprintfCall = (CallExpr) arg;
                        break;
                    }
                }
                
                if (queryArgIdx != -1 && sprintfCall != null && !sprintfCall.getArgs().isEmpty()) {
                    Expr formatStringExpr = sprintfCall.getArgs().get(0);
                    if (formatStringExpr instanceof BasicLit) {
                        BasicLit formatLit = (BasicLit) formatStringExpr;
                        if ("STRING".equals(formatLit.getKind())) {
                            String queryStr = formatLit.getValue();
                            // Replace '%s', '%d', '%v' with '?'
                            // Also handle quotes around strings: '%s' -> ?
                            String newQueryStr = queryStr.replaceAll("'?(%[sdv])'?", "?");
                            
                            BasicLit newFormatLit = formatLit.withValue(newQueryStr);
                            
                            List<Expr> newArgs = new ArrayList<>(c.getArgs());
                            // Remove fmt.Sprintf, replacing with the new format string and its arguments
                            newArgs.set(queryArgIdx, newFormatLit.withPrefix(sprintfCall.getPrefix()));
                            
                            // Add the format arguments to the parent call
                            for (int i = 1; i < sprintfCall.getArgs().size(); i++) {
                                newArgs.add(queryArgIdx + i, sprintfCall.getArgs().get(i));
                            }
                            
                            return c.withArgs(newArgs);
                        }
                    }
                }
                
                return c;
            }
            
            private boolean isFmtSprintf(Expr expr) {
                if (!(expr instanceof CallExpr)) {
                    return false;
                }
                CallExpr call = (CallExpr) expr;
                if (!(call.getFun() instanceof SelectorExpr)) {
                    return false;
                }
                SelectorExpr sel = (SelectorExpr) call.getFun();
                if (!(sel.getX() instanceof Ident)) {
                    return false;
                }
                Ident pkg = (Ident) sel.getX();
                return "fmt".equals(pkg.getName()) && "Sprintf".equals(sel.getSel().getName());
            }
        };
    }
}
