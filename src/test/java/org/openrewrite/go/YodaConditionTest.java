package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class YodaConditionTest {

    @Test
    void swapsYodaCondition() {
        BasicLit fortyTwo = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "42");
        Ident x = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "x", null);
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fortyTwo, "==", x, null);

        YodaCondition recipe = new YodaCondition();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertTrue(resBinary.getX() instanceof Ident);
        assertTrue(resBinary.getY() instanceof BasicLit);
        assertEquals("==", resBinary.getOp());
    }
}
