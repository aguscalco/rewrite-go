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

class UseNamedReturnsTest {

    @Test
    void flagsMultipleUnnamedReturns() {
        Ident intIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        Ident errIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null);
        
        Field ret1 = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), intIdent, null);
        Field ret2 = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), errIdent, null);
        java.util.List<Field> results = Arrays.asList(ret1, ret2);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, results, null);
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Do", null);
        FuncDecl func = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, null);

        UseNamedReturns recipe = new UseNamedReturns();
        Tree result = recipe.getVisitor().visit(func, null);

        assertNotSame(func, result);
        FuncDecl resultFunc = (FuncDecl) result;
        assertTrue(resultFunc.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
