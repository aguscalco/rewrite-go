package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreferFilepathJoinTest {

    @Test
    void migratesConcatToJoin() {
        Ident dirIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "dir", null);
        BasicLit slash = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"/\"");
        BinaryExpr lhs = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, dirIdent, "+", slash, null);
        
        Ident fileIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "file", null);
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, lhs, "+", fileIdent, null);

        PreferFilepathJoin recipe = new PreferFilepathJoin();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr call = (CallExpr) result;
        assertTrue(call.getFun() instanceof SelectorExpr);
        SelectorExpr sel = (SelectorExpr) call.getFun();
        assertEquals("filepath", ((Ident) sel.getX()).getName());
        assertEquals("Join", sel.getSel().getName());
    }
}
