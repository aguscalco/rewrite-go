package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StringsReplaceEmptyTest {

    @Test
    void migratesReplaceEmpty() {
        Ident stringsIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "strings", null);
        Ident replaceIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Replace", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringsIdent, replaceIdent, null);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident oldIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "old", null);
        Ident newIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "new", null);
        BasicLit zeroLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(sIdent, oldIdent, newIdent, zeroLit), false, null);

        StringsReplaceEmpty recipe = new StringsReplaceEmpty();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("s", ((Ident) result).getName());
    }
}
