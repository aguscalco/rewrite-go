package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpRedirectConstantsTest {

    @Test
    void migratesRedirectMagicNumber() {
        Ident httpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "http", null);
        Ident redirectIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Redirect", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, httpIdent, redirectIdent, null);
        
        Ident wIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "w", null);
        Ident rIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "r", null);
        Ident urlIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "url", null);
        BasicLit codeLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "302");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(wIdent, rIdent, urlIdent, codeLit), false, null);

        HttpRedirectConstants recipe = new HttpRedirectConstants();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getArgs().get(3) instanceof SelectorExpr);
        SelectorExpr argSel = (SelectorExpr) resCall.getArgs().get(3);
        assertEquals("http", ((Ident) argSel.getX()).getName());
        assertEquals("StatusFound", argSel.getSel().getName());
    }
}
