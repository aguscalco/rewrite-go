package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathExp1ToETest {

    @Test
    void migratesMathExp1() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident expIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Exp", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, expIdent, null);
        
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "1");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(oneLit), false, null);

        MathExp1ToE recipe = new MathExp1ToE();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof SelectorExpr);
        SelectorExpr resSel = (SelectorExpr) result;
        assertEquals("math", ((Ident) resSel.getX()).getName());
        assertEquals("E", resSel.getSel().getName());
    }
}
