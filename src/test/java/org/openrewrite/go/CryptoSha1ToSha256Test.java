package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CryptoSha1ToSha256Test {

    @Test
    void migratesSha1New() {
        Ident sha1Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sha1", null);
        Ident newIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "New", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sha1Ident, newIdent, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        CryptoSha1ToSha256 recipe = new CryptoSha1ToSha256();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("sha256", ((Ident) resSel.getX()).getName());
        assertEquals("New", resSel.getSel().getName());
    }
}
