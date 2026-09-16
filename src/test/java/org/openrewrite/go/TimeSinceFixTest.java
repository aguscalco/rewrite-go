package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeSinceFixTest {

    @Test
    void convertsTimeNowSubToTimeSince() {
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr timeNowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        CallExpr timeNow = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeNowSel, Collections.emptyList(), false, null);

        Ident subIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sub", null);
        SelectorExpr subSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeNow, subIdent, null);

        Ident arg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, subSel, Collections.singletonList(arg), false, null);

        TimeSinceFix recipe = new TimeSinceFix();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getFun() instanceof SelectorExpr);
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Since", resSel.getSel().getName());
    }
}
