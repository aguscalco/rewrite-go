package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Arrays;

public class UseMapsPackage extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use maps package";
    }
    
    @Override
    public String getDescription() {
        return "Replace manual map operations with maps package functions (Go 1.21+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = callExpr;
                
                // Check if this is a function call that can be replaced with maps package
                if (c.getFun() instanceof Ident) {
                    Ident funcIdent = (Ident) c.getFun();
                    String funcName = funcIdent.getName();
                    
                    // Example: copyMap(m) → maps.Clone(m)
                    if ("copyMap".equals(funcName) && c.getArgs().size() == 1) {
                        return convertCopyMapToMapsClone(c);
                    }
                    
                    // Example: mapEquals(m1, m2) → maps.Equal(m1, m2)
                    if ("mapEquals".equals(funcName) && c.getArgs().size() == 2) {
                        return convertMapEqualsToMapsEqual(c);
                    }
                }
                
                return c;
            }
            
            private CallExpr convertCopyMapToMapsClone(CallExpr callExpr) {
                // copyMap(m) → maps.Clone(m)
                Ident mapsIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "maps",
                    null
                );
                
                Ident cloneIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "Clone",
                    null
                );
                
                SelectorExpr mapsCloneSelector = new SelectorExpr(
                    Tree.randomId(),
                    callExpr.getPrefix(),
                    callExpr.getMarkers(),
                    mapsIdent,
                    cloneIdent,
                    null
                );
                
                return callExpr.withFun(mapsCloneSelector);
            }
            
            private CallExpr convertMapEqualsToMapsEqual(CallExpr callExpr) {
                // mapEquals(m1, m2) → maps.Equal(m1, m2)
                Ident mapsIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "maps",
                    null
                );
                
                Ident equalIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "Equal",
                    null
                );
                
                SelectorExpr mapsEqualSelector = new SelectorExpr(
                    Tree.randomId(),
                    callExpr.getPrefix(),
                    callExpr.getMarkers(),
                    mapsIdent,
                    equalIdent,
                    null
                );
                
                return callExpr.withFun(mapsEqualSelector);
            }
        };
    }
}
