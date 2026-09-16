package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IoUtilTempDirToOsMkdirTempTest {

    @Test
    void migratesTempDir() {
        Ident ioutilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null);
        Ident tempDirIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TempDir", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilIdent, tempDirIdent, null);
        
        BasicLit dirLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"\"");
        BasicLit patternLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"prefix\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(dirLit, patternLit), false, null);

        IoUtilTempDirToOsMkdirTemp recipe = new IoUtilTempDirToOsMkdirTemp();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("os", ((Ident) resSel.getX()).getName());
        assertEquals("MkdirTemp", resSel.getSel().getName());
    }
}
