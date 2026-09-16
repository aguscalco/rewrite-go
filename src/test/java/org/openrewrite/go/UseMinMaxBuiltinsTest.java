package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseMinMaxBuiltinsTest {

    @Test
    void updatesMathMinToMin() {
        Ident mathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "math", null);
        Ident minIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Min", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, mathIdent, minIdent, null);

        Ident arg1 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null);
        Ident arg2 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(arg1, arg2), false, null);

        UseMinMaxBuiltins recipe = new UseMinMaxBuiltins();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resultCall = (CallExpr) result;
        assertTrue(resultCall.getFun() instanceof Ident);
        assertEquals("min", ((Ident) resultCall.getFun()).getName());
    }
}
