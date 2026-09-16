package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesReplaceToReplaceAllTest {

    @Test
    void migratesReplaceAll() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident replaceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Replace", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, replaceIdent, null);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident oldIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "old", null);
        Ident newIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "new", null);
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1");
        UnaryExpr minusOne = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", oneLit, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(sIdent, oldIdent, newIdent, minusOne), false, null);

        BytesReplaceToReplaceAll recipe = new BytesReplaceToReplaceAll();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("ReplaceAll", resSel.getSel().getName());
        assertEquals(3, resCall.getArgs().size());
    }
}
