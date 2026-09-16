package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpStatusConstantsTest {

    @Test
    void migratesWriteHeaderMagicNumber() {
        Ident wIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "w", null);
        Ident writeHeaderIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WriteHeader", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, wIdent, writeHeaderIdent, null);
        
        BasicLit codeLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "404");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(codeLit), false, null);

        HttpStatusConstants recipe = new HttpStatusConstants();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getArgs().get(0) instanceof SelectorExpr);
        SelectorExpr argSel = (SelectorExpr) resCall.getArgs().get(0);
        assertEquals("http", ((Ident) argSel.getX()).getName());
        assertEquals("StatusNotFound", argSel.getSel().getName());
    }
}
