package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PathJoinEmptyStringTest {

    @Test
    void migratesJoinEmptyString() {
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        Ident joinIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Join", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, pathIdent, joinIdent, null);
        
        Ident pIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "p", null);
        BasicLit emptyLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(pIdent, emptyLit), false, null);

        PathJoinEmptyString recipe = new PathJoinEmptyString();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Clean", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
        assertEquals("p", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
