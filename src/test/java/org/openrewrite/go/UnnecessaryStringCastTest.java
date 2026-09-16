package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UnnecessaryStringCastTest {

    @Test
    void migratesStringCast() {
        Ident stringIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null);
        BasicLit stringLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringIdent, Collections.singletonList(stringLit), false, null);

        UnnecessaryStringCast recipe = new UnnecessaryStringCast();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BasicLit);
        assertEquals("\"hello\"", ((BasicLit) result).getValue());
    }
}
