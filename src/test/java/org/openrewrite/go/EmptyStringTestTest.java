package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmptyStringTestTest {

    @Test
    void simplifiesLenEqZero() {
        Ident lenIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "len", null);
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        CallExpr lenCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, lenIdent, Collections.singletonList(sIdent), false, null);
        
        BasicLit zero = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, lenCall, "==", zero, null);

        EmptyStringTest recipe = new EmptyStringTest();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertTrue(resBinary.getX() instanceof Ident);
        assertEquals("s", ((Ident) resBinary.getX()).getName());
        assertTrue(resBinary.getY() instanceof BasicLit);
        assertEquals("\"\"", ((BasicLit) resBinary.getY()).getValue());
    }
}
