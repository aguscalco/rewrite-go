package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SprintfIntToItoaTest {

    @Test
    void migratesSprintfInt() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident sprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sprintf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, sprintfIdent, null);
        
        BasicLit formatLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"%d\"");
        Ident iIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(formatLit, iIdent), false, null);

        SprintfIntToItoa recipe = new SprintfIntToItoa();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("strconv", ((Ident) resSel.getX()).getName());
        assertEquals("Itoa", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
        assertEquals("i", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
