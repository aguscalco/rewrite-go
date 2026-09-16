package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathFloorAddHalfTest {

    @Test
    void migratesMathFloorAddHalf() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident floorIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Floor", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, floorIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        BasicLit halfLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "0.5");
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, xIdent, "+", halfLit, null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(binary), false, null);

        MathFloorAddHalf recipe = new MathFloorAddHalf();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Round", resSel.getSel().getName());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
