package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathPowNeg1Test {

    @Test
    void migratesMathPowNeg1() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident powIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Pow", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, powIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "1");
        UnaryExpr negOne = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", oneLit, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(xIdent, negOne), false, null);

        MathPowNeg1 recipe = new MathPowNeg1();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BinaryExpr);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertEquals("/", resBinary.getOp());
        assertTrue(resBinary.getX() instanceof BasicLit);
        assertEquals("1.0", ((BasicLit) resBinary.getX()).getValue());
        assertTrue(resBinary.getY() instanceof Ident);
        assertEquals("x", ((Ident) resBinary.getY()).getName());
    }
}
