package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MathLog2Test {

    @Test
    void migratesMathLog2() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident logIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Log", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, logIdent, null);
        
        Ident xIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr logX = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(xIdent), false, null);
        
        BasicLit twoLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "FLOAT", "2");
        CallExpr log2 = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(twoLit), false, null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, logX, "/", log2, null);

        MathLog2 recipe = new MathLog2();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Log2", resSel.getSel().getName());
    }
}
