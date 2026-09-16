package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StringBytesStringTest {

    @Test
    void migratesStringBytesString() {
        Ident stringIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null);
        
        Ident byteIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "byte", null);
        ArrayTypeExpr byteSlice = new ArrayTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, byteIdent);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        CallExpr byteCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, byteSlice, Collections.singletonList(sIdent), false, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, stringIdent, Collections.singletonList(byteCall), false, null);

        StringBytesString recipe = new StringBytesString();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof Ident);
        assertEquals("s", ((Ident) result).getName());
    }
}
