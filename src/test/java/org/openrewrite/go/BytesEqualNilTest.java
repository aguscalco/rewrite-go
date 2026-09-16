package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesEqualNilTest {

    @Test
    void migratesEqualNil() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident equalIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Equal", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, equalIdent, null);
        
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident nilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "nil", null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(bIdent, nilIdent), false, null);

        BytesEqualNil recipe = new BytesEqualNil();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BinaryExpr);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertEquals("==", resBinary.getOp());
        assertTrue(resBinary.getX() instanceof CallExpr);
        CallExpr lenCall = (CallExpr) resBinary.getX();
        assertEquals("len", ((Ident) lenCall.getFun()).getName());
        assertEquals("b", ((Ident) lenCall.getArgs().get(0)).getName());
        assertTrue(resBinary.getY() instanceof BasicLit);
        assertEquals("0", ((BasicLit) resBinary.getY()).getValue());
    }
}
