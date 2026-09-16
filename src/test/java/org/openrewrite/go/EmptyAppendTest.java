package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmptyAppendTest {

    @Test
    void migratesEmptyAppend() {
        Ident appendIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "append", null);
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, appendIdent, Collections.singletonList(sIdent), false, null);

        EmptyAppend recipe = new EmptyAppend();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("s", ((Ident) result).getName());
    }
}
