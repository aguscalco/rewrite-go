package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SortFloat64sToSlicesSortTest {

    @Test
    void migratesSortFloat64s() {
        Ident sortIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null);
        Ident float64sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Float64s", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, float64sIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(xIdent), false, null);

        SortFloat64sToSlicesSort recipe = new SortFloat64sToSlicesSort();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("slices", ((Ident) resSel.getX()).getName());
        assertEquals("Sort", resSel.getSel().getName());
    }
}
