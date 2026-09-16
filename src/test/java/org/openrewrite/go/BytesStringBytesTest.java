package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesStringBytesTest {

    @Test
    void migratesBytesStringBytes() {
        Ident byteIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "byte", null);
        ArrayTypeExpr byteSlice = new ArrayTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, byteIdent);
        
        Ident stringIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null);
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        CallExpr stringCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringIdent, Collections.singletonList(bIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, byteSlice, Collections.singletonList(stringCall), false, null);

        BytesStringBytes recipe = new BytesStringBytes();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("b", ((Ident) result).getName());
    }
}
