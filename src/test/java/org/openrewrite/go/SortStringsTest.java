package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SortStringsTest {

    @Test
    void migratesSortStrings() {
        Ident sortIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null);
        Ident sortMethodIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sort", null);
        SelectorExpr sortSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, sortMethodIdent, null);
        
        Ident stringSliceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "StringSlice", null);
        SelectorExpr stringSliceSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, stringSliceIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr stringSliceCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringSliceSel, Collections.singletonList(xIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortSel, Collections.singletonList(stringSliceCall), false, null);

        SortStrings recipe = new SortStrings();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Strings", resSel.getSel().getName());
    }
}
