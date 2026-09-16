package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathLog1pTest {

    @Test
    void migratesMathLog1p() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident logIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Log", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, logIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "1");
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, oneLit, "+", xIdent, null);
        CallExpr logCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(binary), false, null);

        MathLog1p recipe = new MathLog1p();
        Tree result = recipe.getVisitor().visit(logCall, null);

        assertNotSame(logCall, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Log1p", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
        assertEquals("x", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
