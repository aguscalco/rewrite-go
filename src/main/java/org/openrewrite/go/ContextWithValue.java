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
 * Ensures context.WithValue uses type-safe keys by wrapping primitive keys in a custom type.
 *
 * This avoids collisions when multiple packages put data into the same context using string keys.
 */
public class ContextWithValue extends Recipe {

    @Option(displayName = "Key type name",
            description = "The type to wrap primitive context keys in.",
            example = "contextKey")
    String keyTypeName = "contextKey";

    @Override
    public String getDisplayName() {
        return "Use type-safe context keys";
    }

    @Override
    public String getDescription() {
        return "Wraps primitive context keys in a custom type to prevent collisions.";
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
                if (!(sel.getX() instanceof Ident) || !((Ident) sel.getX()).getName().equals("context") || !sel.getSel().getName().equals("WithValue")) {
                    return c;
                }
                
                if (c.getArgs().size() != 3) {
                    return c;
                }
                
                Expr key = c.getArgs().get(1);
                // In Go, untyped literals mapped to BasicLit should not be used as context keys.
                if (key instanceof BasicLit) {
                    Ident typeIdent = new Ident(
                        Tree.randomId(),
                        Space.EMPTY,
                        key.getMarkers(),
                        keyTypeName,
                        null
                    );
                    
                    Space originalPrefix = key.getPrefix();
                    Expr strippedKey = ((BasicLit) key).withPrefix(Space.EMPTY);
                    
                    CallExpr typeCast = new CallExpr(
                        Tree.randomId(),
                        originalPrefix,
                        key.getMarkers(),
                        typeIdent,
                        java.util.Collections.singletonList(strippedKey),
                        false,
                        null
                    );
                    
                    List<Expr> newArgs = new ArrayList<>(c.getArgs());
                    newArgs.set(1, typeCast);
                    return c.withArgs(newArgs);
                }
                
                return c;
            }
        };
    }
}
