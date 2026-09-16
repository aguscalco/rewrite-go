package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtFprintToPrintTest {

    @Test
    void migratesFprintStdout() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident fprintIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Fprintln", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, fprintIdent, null);
        
        Ident osIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident stdoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Stdout", null);
        SelectorExpr osStdout = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osIdent, stdoutIdent, null);
        
        BasicLit stringLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(osStdout, stringLit), false, null);

        FmtFprintToPrint recipe = new FmtFprintToPrint();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Println", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
    }
}
