package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathPowNeg05Test {

    @Test
    void migratesMathPowNeg05() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident powIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Pow", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, powIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        
        BasicLit halfLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "0.5");
        UnaryExpr negHalf = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", halfLit, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(xIdent, negHalf), false, null);

        MathPowNeg05 recipe = new MathPowNeg05();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BinaryExpr);
        BinaryExpr resBinary = (BinaryExpr) result;
        assertEquals("/", resBinary.getOp());
        assertTrue(resBinary.getX() instanceof BasicLit);
        assertEquals("1.0", ((BasicLit) resBinary.getX()).getValue());
        assertTrue(resBinary.getY() instanceof CallExpr);
        CallExpr resCall = (CallExpr) resBinary.getY();
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Sqrt", resSel.getSel().getName());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
