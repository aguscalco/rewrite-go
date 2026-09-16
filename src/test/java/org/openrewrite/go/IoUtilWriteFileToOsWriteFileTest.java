package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilWriteFileToOsWriteFileTest {

    @Test
    void migratesWriteFile() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident writeFileIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WriteFile", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, writeFileIdent, null);
        
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        Ident dataIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null);
        BasicLit permLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0644");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(pathIdent, dataIdent, permLit), false, null);

        IoUtilWriteFileToOsWriteFile recipe = new IoUtilWriteFileToOsWriteFile();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("os", ((Ident) resSel.getX()).getName());
    }
}
