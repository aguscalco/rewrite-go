package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeSinceNowTest {

    @Test
    void migratesTimeSinceNow() {
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident sinceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Since", null);
        SelectorExpr sinceSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, sinceIdent, null);
        
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr nowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        CallExpr nowCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nowSel, Collections.emptyList(), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sinceSel, Collections.singletonList(nowCall), false, null);

        TimeSinceNow recipe = new TimeSinceNow();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BasicLit);
        assertEquals("0", ((BasicLit) result).getValue());
    }
}
