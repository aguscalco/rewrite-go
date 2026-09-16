package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathPow1Test {

    @Test
    void migratesMathPow1() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident powIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Pow", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, powIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(xIdent, oneLit), false, null);

        MathPow1 recipe = new MathPow1();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("x", ((Ident) result).getName());
    }
}
