package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilReadAllToIoReadAllTest {

    @Test
    void migratesReadAll() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident readAllIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ReadAll", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, readAllIdent, null);
        
        Ident rIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "r", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(rIdent), false, null);

        IoUtilReadAllToIoReadAll recipe = new IoUtilReadAllToIoReadAll();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("io", ((Ident) resSel.getX()).getName());
    }
}
