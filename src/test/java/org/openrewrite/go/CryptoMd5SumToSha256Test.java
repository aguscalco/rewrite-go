package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CryptoMd5SumToSha256Test {

    @Test
    void migratesMd5Sum() {
        Ident md5Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "md5", null);
        Ident sumIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sum", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, md5Ident, sumIdent, null);
        
        Ident dataIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(dataIdent), false, null);

        CryptoMd5SumToSha256 recipe = new CryptoMd5SumToSha256();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("sha256", ((Ident) resSel.getX()).getName());
        assertEquals("Sum256", resSel.getSel().getName());
    }
}
