package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesCountToContainsTest {

    @Test
    void migratesCountGreaterZero() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident countIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Count", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, countIdent, null);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sub", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(sIdent, subIdent), false, null);
        
        BasicLit zeroLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call, ">", zeroLit, null);

        BytesCountToContains recipe = new BytesCountToContains();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Contains", resSel.getSel().getName());
    }
}
