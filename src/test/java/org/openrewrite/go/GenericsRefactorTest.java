package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GenericsRefactorTest {

    @Test
    void flagsAnyParameter() {
        Ident anyIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "any", null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("val"), anyIdent, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Process", null);
        FuncDecl func = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, null);

        GenericsRefactor recipe = new GenericsRefactor();
        Tree result = recipe.getVisitor().visit(func, null);

        assertNotSame(func, result);
        FuncDecl resultFunc = (FuncDecl) result;
        assertTrue(resultFunc.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
