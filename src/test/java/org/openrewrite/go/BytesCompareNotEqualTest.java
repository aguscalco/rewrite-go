package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesCompareNotEqualTest {

    @Test
    void migratesCompareNotEqual() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident compareIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Compare", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, compareIdent, null);
        
        Ident aIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(aIdent, bIdent), false, null);
        BasicLit zeroLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call, "!=", zeroLit, null);

        BytesCompareNotEqual recipe = new BytesCompareNotEqual();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof UnaryExpr);
        UnaryExpr resUnary = (UnaryExpr) result;
        assertEquals("!", resUnary.getOp());
        assertTrue(resUnary.getX() instanceof CallExpr);
        CallExpr resCall = (CallExpr) resUnary.getX();
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Equal", resSel.getSel().getName());
    }
}
