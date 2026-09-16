package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeUnixNanoToMicroTest {

    @Test
    void migratesUnixNanoToMicro() {
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident nowIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Now", null);
        SelectorExpr nowSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, nowIdent, null);
        CallExpr nowCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nowSel, Collections.emptyList(), false, null);
        
        Ident nanoIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "UnixNano", null);
        SelectorExpr nanoSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nowCall, nanoIdent, null);
        CallExpr nanoCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nanoSel, Collections.emptyList(), false, null);
        
        BasicLit thousandLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1000");
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, nanoCall, "/", thousandLit, null);

        TimeUnixNanoToMicro recipe = new TimeUnixNanoToMicro();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("UnixMicro", resSel.getSel().getName());
    }
}
