package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PropagateContextTest {
    
    @Test
    void doesNotModifyCallAlreadyWithContext() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "processData", null),
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null)
            ),
            false,
            null
        );
        
        PropagateContext recipe = new PropagateContext();
        CallExpr result = (CallExpr) recipe.getVisitor().visit(callExpr, null);
        
        assertSame(callExpr, result);
        assertEquals(2, result.getArgs().size());
    }
    
    @Test
    void doesNotModifyEmptyCall() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "getData", null),
            Collections.emptyList(),
            false,
            null
        );
        
        PropagateContext recipe = new PropagateContext();
        CallExpr result = (CallExpr) recipe.getVisitor().visit(callExpr, null);
        
        assertSame(callExpr, result);
        assertEquals(0, result.getArgs().size());
    }
    
    @Test
    void doesNotModifyCallWithContextLikeArg() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "handle", null),
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "requestCtx", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "req", null)
            ),
            false,
            null
        );
        
        PropagateContext recipe = new PropagateContext();
        CallExpr result = (CallExpr) recipe.getVisitor().visit(callExpr, null);
        
        assertSame(callExpr, result);
        assertEquals(2, result.getArgs().size());
    }
    
    @Test
    void placeholderRecipeDoesNotCrash() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "doSomething", null),
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "arg1", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "arg2", null)
            ),
            false,
            null
        );
        
        PropagateContext recipe = new PropagateContext();
        CallExpr result = (CallExpr) recipe.getVisitor().visit(callExpr, null);
        
        // Placeholder implementation returns unchanged
        assertNotNull(result);
        assertEquals(2, result.getArgs().size());
    }
}
