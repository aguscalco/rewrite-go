package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Arrays;

public class UseErrorsIs extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use errors.Is";
    }
    
    @Override
    public String getDescription() {
        return "Replace direct error comparison (err == target) with errors.Is(err, target).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = binaryExpr;
                
                // Check if this is an equality comparison (== or !=)
                if ("==".equals(b.getOp()) || "!=".equals(b.getOp())) {
                    // Check if one side is an error variable and the other is a sentinel error
                    if (isErrorComparison(b.getX(), b.getY())) {
                        return convertToErrorsIs(b);
                    }
                    if (isErrorComparison(b.getY(), b.getX())) {
                        // Swap operands for errors.Is
                        BinaryExpr swapped = b.withX(b.getY()).withY(b.getX());
                        return convertToErrorsIs(swapped);
                    }
                }
                
                return b;
            }
            
            private boolean isErrorComparison(Expr left, Expr right) {
                // Check if left is an identifier that could be an error
                // and right is a sentinel error (like io.EOF, custom errors, etc.)
                if (left instanceof Ident) {
                    Ident leftIdent = (Ident) left;
                    // Check if it's named "err" or "e" (common error variable names)
                    if ("err".equals(leftIdent.getName()) || "e".equals(leftIdent.getName())) {
                        // Check if right is a selector expression (package.Error)
                        if (right instanceof SelectorExpr) {
                            return true;
                        }
                        // Or if right is an identifier (could be a sentinel error)
                        if (right instanceof Ident) {
                            Ident rightIdent = (Ident) right;
                            // Check if it looks like a sentinel error (starts with Err or EOF, etc.)
                            // But NOT nil - nil checks should remain as-is
                            String name = rightIdent.getName();
                            return (name.startsWith("Err") || "EOF".equals(name)) && !"nil".equals(name);
                        }
                    }
                }
                return false;
            }
            
            private CallExpr convertToErrorsIs(BinaryExpr binaryExpr) {
                // Create errors.Is(err, target)
                Ident errorsIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    binaryExpr.getMarkers(),
                    "errors",
                    null
                );
                
                Ident isIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    binaryExpr.getMarkers(),
                    "Is",
                    null
                );
                
                SelectorExpr errorsIsSelector = new SelectorExpr(
                    Tree.randomId(),
                    binaryExpr.getPrefix(),
                    binaryExpr.getMarkers(),
                    errorsIdent,
                    isIdent,
                    null
                );
                
                // For != comparison, we need to wrap with !
                if ("!=".equals(binaryExpr.getOp())) {
                    // Create !errors.Is(err, target)
                    CallExpr errorsIsCall = new CallExpr(
                        Tree.randomId(),
                        Space.EMPTY,
                        binaryExpr.getMarkers(),
                        errorsIsSelector,
                        Arrays.asList(binaryExpr.getX(), binaryExpr.getY()),
                        false,
                        null
                    );
                    
                    return new CallExpr(
                        Tree.randomId(),
                        binaryExpr.getPrefix(),
                        binaryExpr.getMarkers(),
                        new Ident(Tree.randomId(), Space.EMPTY, binaryExpr.getMarkers(), "!", null),
                        java.util.Collections.singletonList(errorsIsCall),
                        false,
                        null
                    );
                }
                
                // For == comparison, just use errors.Is(err, target)
                return new CallExpr(
                    Tree.randomId(),
                    binaryExpr.getPrefix(),
                    binaryExpr.getMarkers(),
                    errorsIsSelector,
                    Arrays.asList(binaryExpr.getX(), binaryExpr.getY()),
                    false,
                    null
                );
            }
        };
    }
}
