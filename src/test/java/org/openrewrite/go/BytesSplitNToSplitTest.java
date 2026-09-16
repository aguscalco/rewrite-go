package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BytesSplitNToSplitTest {

    @Test
    void migratesBytesSplitN() {
        Ident bytesIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "bytes", null);
        Ident splitNIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "SplitN", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bytesIdent, splitNIdent, null);
        
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident sepIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sep", null);
        BasicLit oneLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1");
        UnaryExpr negOne = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "-", oneLit, null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(sIdent, sepIdent, negOne), false, null);

        BytesSplitNToSplit recipe = new BytesSplitNToSplit();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Split", resSel.getSel().getName());
        assertEquals(2, resCall.getArgs().size());
    }
}
