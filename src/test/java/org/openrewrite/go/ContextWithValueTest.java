package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContextWithValueTest {

    @Test
    void wrapsStringLiteralKey() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withValueIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithValue", null);
        SelectorExpr withValueSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, contextIdent, withValueIdent, null);
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        BasicLit keyLit = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "STRING", "\"myKey\"");
        Ident valIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "val", null);
        
        CallExpr call = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withValueSel,
            Arrays.asList(ctxIdent, keyLit, valIdent),
            false,
            null
        );
        
        ContextWithValue recipe = new ContextWithValue();
        recipe.keyTypeName = "ContextKey";
        
        Tree result = recipe.getVisitor().visit(call, null);
        
        assertNotSame(call, result);
        CallExpr resultCall = (CallExpr) result;
        
        Expr newKey = resultCall.getArgs().get(1);
        assertInstanceOf(CallExpr.class, newKey);
        CallExpr keyCast = (CallExpr) newKey;
        
        assertInstanceOf(Ident.class, keyCast.getFun());
        assertEquals("ContextKey", ((Ident) keyCast.getFun()).getName());
        assertEquals(" ", keyCast.getPrefix().getWhitespace());
        
        Expr innerKey = keyCast.getArgs().get(0);
        assertInstanceOf(BasicLit.class, innerKey);
        assertEquals("\"myKey\"", ((BasicLit) innerKey).getValue());
        assertEquals("", innerKey.getPrefix().getWhitespace());
    }

    @Test
    void ignoresAlreadyTypedKeys() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withValueIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithValue", null);
        SelectorExpr withValueSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, contextIdent, withValueIdent, null);
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        
        // key is already a variable (Ident), not a BasicLit
        Ident keyIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "myTypedKey", null);
        Ident valIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "val", null);
        
        CallExpr call = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withValueSel,
            Arrays.asList(ctxIdent, keyIdent, valIdent),
            false,
            null
        );
        
        ContextWithValue recipe = new ContextWithValue();
        recipe.keyTypeName = "ContextKey";
        
        Tree result = recipe.getVisitor().visit(call, null);
        
        // Should not modify the call since the key is not a primitive BasicLit
        assertSame(call, result);
    }
}
