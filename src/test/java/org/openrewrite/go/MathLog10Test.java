package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathLog10Test {

    @Test
    void migratesMathLog10() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident logIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Log", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, logIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr logX = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(xIdent), false, null);
        
        BasicLit tenLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "10");
        CallExpr log10 = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(tenLit), false, null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, logX, "/", log10, null);

        MathLog10 recipe = new MathLog10();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Log10", resSel.getSel().getName());
    }
}
