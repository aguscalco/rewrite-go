package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReplaceContextTODOTest {

    @Test
    void replacesContextTODO() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new SelectorExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TODO", null),
                null
            ),
            Collections.emptyList(),
            false,
            null
        );

        ReplaceContextTODO recipe = new ReplaceContextTODO();
        Expr result = (Expr) recipe.getVisitor().visit(callExpr, null);

        assertInstanceOf(Ident.class, result);
        assertEquals("ctx", ((Ident) result).getName());
    }

    @Test
    void doesNotReplaceOtherContextMethods() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new SelectorExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null),
                null
            ),
            Collections.emptyList(),
            false,
            null
        );

        ReplaceContextTODO recipe = new ReplaceContextTODO();
        Expr result = (Expr) recipe.getVisitor().visit(callExpr, null);

        assertInstanceOf(CallExpr.class, result);
        assertSame(callExpr, result);
    }

    @Test
    void doesNotReplaceNonContextPackage() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new SelectorExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "other", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TODO", null),
                null
            ),
            Collections.emptyList(),
            false,
            null
        );

        ReplaceContextTODO recipe = new ReplaceContextTODO();
        Expr result = (Expr) recipe.getVisitor().visit(callExpr, null);

        assertInstanceOf(CallExpr.class, result);
        assertSame(callExpr, result);
    }

    @Test
    void preservesPrefixAndMarkers() {
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.build("  "),
            Markers.EMPTY,
            new SelectorExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TODO", null),
                null
            ),
            Collections.emptyList(),
            false,
            null
        );

        ReplaceContextTODO recipe = new ReplaceContextTODO();
        Expr result = (Expr) recipe.getVisitor().visit(callExpr, null);

        assertInstanceOf(Ident.class, result);
        assertEquals("  ", result.getPrefix().getWhitespace());
        assertEquals(Markers.EMPTY, result.getMarkers());
    }
}
