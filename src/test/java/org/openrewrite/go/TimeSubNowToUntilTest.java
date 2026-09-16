package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeSubNowToUntilTest {

    @Test
    void migratesTimeSubNow() {
        Ident tIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sub", null);
        SelectorExpr subSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tIdent, subIdent, null);
        
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr nowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        
        CallExpr nowCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nowSel, Collections.emptyList(), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, subSel, Collections.singletonList(nowCall), false, null);

        TimeSubNowToUntil recipe = new TimeSubNowToUntil();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("time", ((Ident) resSel.getX()).getName());
        assertEquals("Until", resSel.getSel().getName());
        assertEquals("t", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
