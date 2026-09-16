package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpErrorConstantsTest {

    @Test
    void migratesErrorMagicNumber() {
        Ident httpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "http", null);
        Ident errorIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Error", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, httpIdent, errorIdent, null);
        
        Ident wIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "w", null);
        Ident errIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null);
        BasicLit codeLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "500");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(wIdent, errIdent, codeLit), false, null);

        HttpErrorConstants recipe = new HttpErrorConstants();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getArgs().get(2) instanceof SelectorExpr);
        SelectorExpr argSel = (SelectorExpr) resCall.getArgs().get(2);
        assertEquals("http", ((Ident) argSel.getX()).getName());
        assertEquals("StatusInternalServerError", argSel.getSel().getName());
    }
}
