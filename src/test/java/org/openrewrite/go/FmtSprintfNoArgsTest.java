package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtSprintfNoArgsTest {

    @Test
    void migratesSprintfNoArgs() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident sprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sprintf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, sprintfIdent, null);
        
        BasicLit stringLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(stringLit), false, null);

        FmtSprintfNoArgs recipe = new FmtSprintfNoArgs();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof BasicLit);
        BasicLit resLit = (BasicLit) result;
        assertEquals("\"hello\"", resLit.getValue());
    }
}
