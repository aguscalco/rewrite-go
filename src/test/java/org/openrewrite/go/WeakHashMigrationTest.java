package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WeakHashMigrationTest {

    @Test
    void migratesMd5ToSha256() {
        Ident md5Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "md5", null);
        Ident newIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "New", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, md5Ident, newIdent, null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        WeakHashMigration recipe = new WeakHashMigration();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getFun() instanceof SelectorExpr);
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("sha256", ((Ident) resSel.getX()).getName());
    }
}
