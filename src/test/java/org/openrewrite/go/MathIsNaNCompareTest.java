package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathIsNaNCompareTest {

    @Test
    void migratesMathNaN() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident nanIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "NaN", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, nanIdent, null);
        CallExpr nanCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, xIdent, "==", nanCall, null);

        MathIsNaNCompare recipe = new MathIsNaNCompare();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("IsNaN", resSel.getSel().getName());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
