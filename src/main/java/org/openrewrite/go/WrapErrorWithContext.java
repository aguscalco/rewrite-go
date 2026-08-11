package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class WrapErrorWithContext extends Recipe {
    
    @Option(displayName = "Context message",
            description = "The context message to wrap the error with.",
            example = "failed to process request")
    private String contextMessage;
    
    @Override
    public String getDisplayName() {
        return "Wrap errors with context";
    }
    
    @Override
    public String getDescription() {
        return "Wrap Go errors with fmt.Errorf to add context using %w verb.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitReturnStmt(ReturnStmt returnStmt, ExecutionContext ctx) {
                if (returnStmt.getResults().size() != 1) {
                    return returnStmt;
                }
                
                Expr result = returnStmt.getResults().get(0);
                
                if (!(result instanceof Ident)) {
                    return returnStmt;
                }
                
                Ident ident = (Ident) result;
                if (!"err".equals(ident.getName())) {
                    return returnStmt;
                }
                
                Ident fmtIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    ident.getMarkers(),
                    "fmt",
                    null
                );
                
                Ident errorfIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    ident.getMarkers(),
                    "Errorf",
                    null
                );
                
                SelectorExpr selector = new SelectorExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    ident.getMarkers(),
                    fmtIdent,
                    errorfIdent,
                    null
                );
                
                BasicLit formatString = new BasicLit(
                    Tree.randomId(),
                    Space.EMPTY,
                    ident.getMarkers(),
                    "STRING",
                    "\"" + contextMessage + ": %w\""
                );
                
                Ident errArg = new Ident(
                    Tree.randomId(),
                    Space.build(" "),
                    ident.getMarkers(),
                    "err",
                    null
                );
                
                java.util.List<Expr> args = new java.util.ArrayList<>();
                args.add(formatString);
                args.add(errArg);
                
                CallExpr callExpr = new CallExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    ident.getMarkers(),
                    selector,
                    args,
                    false,
                    null
                );
                
                java.util.List<Expr> newResults = new java.util.ArrayList<>();
                newResults.add(callExpr);
                
                return returnStmt.withResults(newResults);
            }
        };
    }
}
