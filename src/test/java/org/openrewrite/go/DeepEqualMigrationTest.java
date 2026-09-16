package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeepEqualMigrationTest {

    @Test
    void convertsReflectDeepEqual() {
        Ident reflectIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "reflect", null);
        Ident deepEqualIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "DeepEqual", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, reflectIdent, deepEqualIdent, null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        DeepEqualMigration recipe = new DeepEqualMigration();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getFun() instanceof SelectorExpr);
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("cmp", ((Ident) resSel.getX()).getName());
        assertEquals("Equal", resSel.getSel().getName());
    }
}
