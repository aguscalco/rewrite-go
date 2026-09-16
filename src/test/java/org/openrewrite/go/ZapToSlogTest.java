package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ZapToSlogTest {

    @Test
    void migratesZapInfo() {
        Ident zapIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "zap", null);
        Ident lIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "L", null);
        SelectorExpr innerSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, zapIdent, lIdent, null);
        CallExpr innerCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, innerSel, Collections.emptyList(), false, null);
        
        Ident infoIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Info", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, innerCall, infoIdent, null);
        
        BasicLit msg = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(msg), false, null);

        ZapToSlog recipe = new ZapToSlog();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("slog", ((Ident) resSel.getX()).getName());
        assertEquals("Info", resSel.getSel().getName());
    }
}
