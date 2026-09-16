package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathAbsNegTest {

    @Test
    void migratesMathAbsNeg() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident absIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Abs", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, absIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        UnaryExpr negX = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", xIdent, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(negX), false, null);

        MathAbsNeg recipe = new MathAbsNeg();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getArgs().get(0) instanceof Ident);
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
