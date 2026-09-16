package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GormAutoMigrateCheckTest {

    @Test
    void flagsAutoMigrate() {
        Ident dbIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "db", null);
        Ident autoMigrateIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "AutoMigrate", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, dbIdent, autoMigrateIdent, null);
        
        Ident modelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "User{}", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(modelIdent), false, null);

        GormAutoMigrateCheck recipe = new GormAutoMigrateCheck();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
