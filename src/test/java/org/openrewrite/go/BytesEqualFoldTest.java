package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesEqualFoldTest {

    @Test
    void migratesEqualFold() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident equalIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Equal", null);
        SelectorExpr equalSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, equalIdent, null);
        
        Ident toUpperIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ToUpper", null);
        SelectorExpr toUpperSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, toUpperIdent, null);
        
        Ident aIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        
        CallExpr callA = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, toUpperSel, Collections.singletonList(aIdent), false, null);
        CallExpr callB = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, toUpperSel, Collections.singletonList(bIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, equalSel, Arrays.asList(callA, callB), false, null);

        BytesEqualFold recipe = new BytesEqualFold();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("EqualFold", resSel.getSel().getName());
        assertEquals("a", ((Ident) resCall.getArgs().get(0)).getName());
        assertEquals("b", ((Ident) resCall.getArgs().get(1)).getName());
    }
}
