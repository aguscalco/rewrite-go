package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeSubCompareTest {

    @Test
    void migratesSubToAfter() {
        Ident t1Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t1", null);
        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sub", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, t1Ident, subIdent, null);
        
        Ident t2Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t2", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(t2Ident), false, null);
        
        BasicLit zero = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call, ">", zero, null);

        TimeSubCompare recipe = new TimeSubCompare();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("After", resSel.getSel().getName());
    }
}
