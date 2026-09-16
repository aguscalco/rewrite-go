package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StringsCompareGreaterThanTest {

    @Test
    void migratesStringsCompareGreaterThan() {
        Ident stringsIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "strings", null);
        Ident compareIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Compare", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringsIdent, compareIdent, null);
        
        Ident aIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(aIdent, bIdent), false, null);
        
        BasicLit zeroLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call, ">", zeroLit, null);

        StringsCompareGreaterThan recipe = new StringsCompareGreaterThan();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof BinaryExpr);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertEquals(">", resBinary.getOp());
        assertTrue(resBinary.getX() instanceof Ident);
        assertEquals("a", ((Ident) resBinary.getX()).getName());
        assertTrue(resBinary.getY() instanceof Ident);
        assertEquals("b", ((Ident) resBinary.getY()).getName());
    }
}
