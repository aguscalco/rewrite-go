package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeUntilFixTest {

    @Test
    void convertsTSubTimeNowToTimeUntil() {
        Ident tIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sub", null);
        SelectorExpr subSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tIdent, subIdent, null);

        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr timeNowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        CallExpr timeNow = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeNowSel, Collections.emptyList(), false, null);

        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, subSel, Collections.singletonList(timeNow), false, null);

        TimeUntilFix recipe = new TimeUntilFix();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getFun() instanceof SelectorExpr);
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Until", resSel.getSel().getName());
        assertTrue(resCall.getArgs().get(0) instanceof Ident);
        assertEquals("t", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
