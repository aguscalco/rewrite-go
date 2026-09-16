package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DupArgSimplifyTest {

    @Test
    void simplifiesMathMax() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident maxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Max", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, maxIdent, null);
        
        Ident arg1 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        Ident arg2 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(arg1, arg2), false, null);

        DupArgSimplify recipe = new DupArgSimplify();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("x", ((Ident) result).getName());
    }
}
