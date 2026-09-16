package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GorillaToStdlibTest {

    @Test
    void migratesNewRouter() {
        Ident muxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "mux", null);
        Ident newRouterIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "NewRouter", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, muxIdent, newRouterIdent, null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        GorillaToStdlib recipe = new GorillaToStdlib();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("http", ((Ident) resSel.getX()).getName());
        assertEquals("NewServeMux", resSel.getSel().getName());
    }
}
