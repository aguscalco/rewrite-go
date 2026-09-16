package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathExpm1Test {

    @Test
    void migratesMathExpm1() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident expIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Exp", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, expIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr expX = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(xIdent), false, null);
        
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "1");
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, expX, "-", oneLit, null);

        MathExpm1 recipe = new MathExpm1();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Expm1", resSel.getSel().getName());
    }
}
