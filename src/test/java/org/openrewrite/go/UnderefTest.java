package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UnderefTest {

    @Test
    void simplifiesStarAmpersand() {
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        UnaryExpr amp = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "&", xIdent, null);
        StarExpr star = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, amp, null);

        Underef recipe = new Underef();
        Tree result = recipe.getVisitor().visit(star, null);

        assertNotSame(star, result);
        assertTrue(result instanceof Ident);
        assertEquals("x", ((Ident) result).getName());
    }
}
