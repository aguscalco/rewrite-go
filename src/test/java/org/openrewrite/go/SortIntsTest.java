package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SortIntsTest {

    @Test
    void migratesSortInts() {
        Ident sortIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null);
        Ident sortMethodIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sort", null);
        SelectorExpr sortSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, sortMethodIdent, null);
        
        Ident intSliceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "IntSlice", null);
        SelectorExpr intSliceSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortIdent, intSliceIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr intSliceCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, intSliceSel, Collections.singletonList(xIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sortSel, Collections.singletonList(intSliceCall), false, null);

        SortInts recipe = new SortInts();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Ints", resSel.getSel().getName());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
