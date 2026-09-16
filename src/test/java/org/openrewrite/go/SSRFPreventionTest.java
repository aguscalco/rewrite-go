package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SSRFPreventionTest {

    @Test
    void flagsDynamicHttpGet() {
        Ident httpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "http", null);
        Ident getIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Get", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, httpIdent, getIdent, null);
        
        Ident urlIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "userInputUrl", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(urlIdent), false, null);

        SSRFPrevention recipe = new SSRFPrevention();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        assertTrue(resCall.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }

    @Test
    void allowsStaticHttpGet() {
        Ident httpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "http", null);
        Ident getIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Get", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, httpIdent, getIdent, null);
        
        BasicLit urlLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"https://api.github.com/\"");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(urlLit), false, null);

        SSRFPrevention recipe = new SSRFPrevention();
        Tree result = recipe.getVisitor().visit(call, null);

        assertSame(call, result);
    }
}
