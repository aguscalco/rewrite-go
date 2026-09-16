package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtFprintfNoArgsTest {

    @Test
    void migratesFprintfNoArgs() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident fprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Fprintf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, fprintfIdent, null);
        
        Ident wIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "w", null);
        BasicLit stringLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(wIdent, stringLit), false, null);

        FmtFprintfNoArgs recipe = new FmtFprintfNoArgs();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Fprint", resSel.getSel().getName());
    }
}
