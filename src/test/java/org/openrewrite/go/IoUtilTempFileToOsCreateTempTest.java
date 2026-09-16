package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilTempFileToOsCreateTempTest {

    @Test
    void migratesTempFile() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident tempFileIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TempFile", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, tempFileIdent, null);
        
        BasicLit dirLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"\"");
        BasicLit patternLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"prefix\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(dirLit, patternLit), false, null);

        IoUtilTempFileToOsCreateTemp recipe = new IoUtilTempFileToOsCreateTemp();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("os", ((Ident) resSel.getX()).getName());
        assertEquals("CreateTemp", resSel.getSel().getName());
    }
}
