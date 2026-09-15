package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpServeMuxRoutingTest {

    @Test
    void flagsMissingVerb() {
        Ident httpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "http", null);
        Ident handleFunc = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "HandleFunc", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, httpIdent, handleFunc, null);
        
        BasicLit path = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"/api/users\"");
        Ident handler = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "userHandler", null);
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(path, handler), false, null);

        HttpServeMuxRouting recipe = new HttpServeMuxRouting();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resultCall = (CallExpr) result;
        assertTrue(resultCall.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
