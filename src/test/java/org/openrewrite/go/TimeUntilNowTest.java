package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeUntilNowTest {

    @Test
    void migratesTimeUntilNow() {
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident untilIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Until", null);
        SelectorExpr untilSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, untilIdent, null);
        
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr nowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        CallExpr nowCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nowSel, Collections.emptyList(), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, untilSel, Collections.singletonList(nowCall), false, null);

        TimeUntilNow recipe = new TimeUntilNow();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BasicLit);
        assertEquals("0", ((BasicLit) result).getValue());
    }
}
