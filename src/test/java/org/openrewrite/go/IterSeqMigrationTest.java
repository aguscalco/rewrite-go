package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IterSeqMigrationTest {

    @Test
    void flagsChannelReturns() {
        Ident valType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        ChanTypeExpr chanType = new ChanTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, 1, valType);
        
        Field returnField = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, chanType, null);

        IterSeqMigration recipe = new IterSeqMigration();
        Tree result = recipe.getVisitor().visit(returnField, null);

        assertNotSame(returnField, result);
        Field resultField = (Field) result;
        assertTrue(resultField.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
