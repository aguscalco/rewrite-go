package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SortStringsAreSortedTest {

    @Test
    void migratesStringsAreSorted() {
        Ident sortIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null);
        Ident isSortedIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "IsSorted", null);
        SelectorExpr isSortedSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, isSortedIdent, null);
        
        Ident stringSliceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "StringSlice", null);
        SelectorExpr stringSliceSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, stringSliceIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr sliceCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringSliceSel, Collections.singletonList(xIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, isSortedSel, Collections.singletonList(sliceCall), false, null);

        SortStringsAreSorted recipe = new SortStringsAreSorted();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("StringsAreSorted", resSel.getSel().getName());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
