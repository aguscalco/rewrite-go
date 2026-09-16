package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BoolExprSimplifyTest {

    @Test
    void simplifiesNotEqual() {
        Ident aIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, aIdent, "==", bIdent, null);
        
        UnaryExpr unary = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "!", binary, null);

        BoolExprSimplify recipe = new BoolExprSimplify();
        Tree result = recipe.getVisitor().visit(unary, null);

        assertNotSame(unary, result);
        assertTrue(result instanceof BinaryExpr);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertEquals("!=", resBinary.getOp());
    }
}
