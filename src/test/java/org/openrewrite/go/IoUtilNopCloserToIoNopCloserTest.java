package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilNopCloserToIoNopCloserTest {

    @Test
    void migratesNopCloser() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident nopCloserIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "NopCloser", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, nopCloserIdent, null);
        
        Ident rIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "r", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(rIdent), false, null);

        IoUtilNopCloserToIoNopCloser recipe = new IoUtilNopCloserToIoNopCloser();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("io", ((Ident) resSel.getX()).getName());
    }
}
