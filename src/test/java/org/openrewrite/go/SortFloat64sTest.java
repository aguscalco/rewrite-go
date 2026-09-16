package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SortFloat64sTest {

    @Test
    void migratesSortFloat64s() {
        Ident sortIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null);
        Ident sortMethodIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sort", null);
        SelectorExpr sortSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, sortMethodIdent, null);
        
        Ident float64SliceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Float64Slice", null);
        SelectorExpr float64SliceSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, float64SliceIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr float64SliceCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, float64SliceSel, Collections.singletonList(xIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortSel, Collections.singletonList(float64SliceCall), false, null);

        SortFloat64s recipe = new SortFloat64s();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Float64s", resSel.getSel().getName());
    }
}
