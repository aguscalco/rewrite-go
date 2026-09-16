package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DoubleNegationTest {

    @Test
    void migratesDoubleNegation() {
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        UnaryExpr innerNot = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "!", bIdent, null);
        UnaryExpr outerNot = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "!", innerNot, null);

        DoubleNegation recipe = new DoubleNegation();
        Tree result = recipe.getVisitor().visit(outerNot, null);

        assertNotSame(outerNot, result);
        assertTrue(result instanceof Ident);
        assertEquals("b", ((Ident) result).getName());
    }
}
