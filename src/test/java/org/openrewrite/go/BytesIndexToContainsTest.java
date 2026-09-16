package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesIndexToContainsTest {

    @Test
    void migratesIndexNotMinusOne() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident indexIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Index", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, indexIdent, null);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sub", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(sIdent, subIdent), false, null);
        
        BasicLit one = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1");
        UnaryExpr minusOne = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", one, null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call, "!=", minusOne, null);

        BytesIndexToContains recipe = new BytesIndexToContains();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Contains", resSel.getSel().getName());
    }
}
