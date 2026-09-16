package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilReadFileToOsReadFileTest {

    @Test
    void migratesReadFile() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident readFileIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ReadFile", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, readFileIdent, null);
        
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(pathIdent), false, null);

        IoUtilReadFileToOsReadFile recipe = new IoUtilReadFileToOsReadFile();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("os", ((Ident) resSel.getX()).getName());
    }
}
