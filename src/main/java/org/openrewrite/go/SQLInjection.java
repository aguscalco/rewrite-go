package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects and remediates simple SQL injection patterns using string concatenation.
 * 
 * Example:
 * db.Query("SELECT * FROM users WHERE name = '" + name + "'")
 * // becomes
 * db.Query("SELECT * FROM users WHERE name = '?'", name) 
 * (Note: Quotes around ? would then be handled by another pass or the developer,
 * but this breaks the immediate concatenation injection.)
 */
public class SQLInjection extends Recipe {

    @Override
    public String getDisplayName() {
        return "Fix SQL injection from string concatenation";
    }

    @Override
    public String getDescription() {
        return "Migrates simple string concatenations in SQL queries to parameterized variables.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
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
                
                // For simplicity, look at the first argument and see if it's a BinaryExpr with '+'
                Expr queryArg = c.getArgs().get(0);
                if (queryArg instanceof BinaryExpr) {
                    BinaryExpr bin = (BinaryExpr) queryArg;
                    if ("+".equals(bin.getOp())) {
                        // Very naive approach: we take the left side if it's a string, and add the right side as a parameter
                        if (bin.getX() instanceof BasicLit && ((BasicLit) bin.getX()).getKind().equals("STRING")) {
                            BasicLit leftStr = (BasicLit) bin.getX();
                            
                            // Remove the closing quote, add ?, and append closing quote
                            String val = leftStr.getValue();
                            if (val.endsWith("\"")) {
                                val = val.substring(0, val.length() - 1) + "?\"";
                            }
                            
                            BasicLit newLeft = leftStr.withValue(val);
                            
                            List<Expr> newArgs = new ArrayList<>();
                            newArgs.add(newLeft.withPrefix(queryArg.getPrefix()));
                            newArgs.add(bin.getY() instanceof Go ? (Expr)((Go)bin.getY()).withPrefix(Space.build(" ")) : bin.getY());
                            
                            // Append any other arguments that were already there
                            for (int i = 1; i < c.getArgs().size(); i++) {
                                newArgs.add(c.getArgs().get(i));
                            }
                            
                            return c.withArgs(newArgs);
                        }
                    }
                }
                
                return c;
            }
        };
    }
}
