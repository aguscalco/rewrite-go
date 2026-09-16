package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FilepathJoinEmptyStringTest {

    @Test
    void migratesJoinEmptyString() {
        Ident filepathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "filepath", null);
        Ident joinIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Join", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, filepathIdent, joinIdent, null);
        
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        BasicLit emptyLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(pathIdent, emptyLit), false, null);

        FilepathJoinEmptyString recipe = new FilepathJoinEmptyString();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Clean", resSel.getSel().getName());
        assertEquals(1, resCall.getArgs().size());
        assertEquals("path", ((Ident) resCall.getArgs().get(0)).getName());
    }
}
