package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AtomicPointerMigrationTest {

    @Test
    void flagsAtomicValue() {
        Ident atomicIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "atomic", null);
        Ident valueIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Value", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, atomicIdent, valueIdent, null);

        AtomicPointerMigration recipe = new AtomicPointerMigration();
        Tree result = recipe.getVisitor().visit(sel, null);

        assertNotSame(sel, result);
        assertTrue(result.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
