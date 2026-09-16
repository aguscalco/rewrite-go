package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LogrusToSlogTest {

    @Test
    void migratesLogrusInfo() {
        Ident logrusIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "logrus", null);
        Ident infoIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Infof", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, logrusIdent, infoIdent, null);
        
        BasicLit msg = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello %s\"");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(msg), false, null);

        LogrusToSlog recipe = new LogrusToSlog();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("slog", ((Ident) resSel.getX()).getName());
        assertEquals("Info", resSel.getSel().getName());
    }
}
