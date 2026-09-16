package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtSprintfVToSprintTest {

    @Test
    void migratesSprintfV() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident sprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sprintf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, sprintfIdent, null);
        
        BasicLit formatLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"%v\"");
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(formatLit, xIdent), false, null);

        FmtSprintfVToSprint recipe = new FmtSprintfVToSprint();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Sprint", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
