package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StringsEqualFoldToBytesTest {

    @Test
    void migratesEqualFold() {
        Ident stringsIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "strings", null);
        Ident equalFoldIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "EqualFold", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringsIdent, equalFoldIdent, null);
        
        Ident stringIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null);
        Ident aIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        
        CallExpr castA = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringIdent, Collections.singletonList(aIdent), false, null);
        CallExpr castB = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringIdent, Collections.singletonList(bIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(castA, castB), false, null);

        StringsEqualFoldToBytes recipe = new StringsEqualFoldToBytes();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("bytes", ((Ident) resSel.getX()).getName());
        assertEquals("a", ((Ident) resCall.getArgs().get(0)).getName());
        assertEquals("b", ((Ident) resCall.getArgs().get(1)).getName());
    }
}
