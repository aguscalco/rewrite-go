package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtErrorfFmtSprintfTest {

    @Test
    void migratesFmtErrorfFmtSprintf() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident errorfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Errorf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, errorfIdent, null);
        
        Ident sprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sprintf", null);
        SelectorExpr sprintfSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, sprintfIdent, null);
        
        BasicLit formatLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"error: %v\"");
        Ident errIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null);
        CallExpr sprintfCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sprintfSel, Arrays.asList(formatLit, errIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(sprintfCall), false, null);

        FmtErrorfFmtSprintf recipe = new FmtErrorfFmtSprintf();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("fmt", ((Ident) resSel.getX()).getName());
        assertEquals("Errorf", resSel.getSel().getName());
        assertEquals(2, resCall.getArgs().size());
    }
}
