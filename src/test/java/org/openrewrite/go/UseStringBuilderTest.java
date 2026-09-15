package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseStringBuilderTest {

    @Test
    void flagsConcat() {
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident vIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "v", null);
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(sIdent), "+=", Collections.singletonList(vIdent));
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(assign), Space.build("\n"));
        
        RangeStmt loop = new RangeStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, 
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "_", null),
                vIdent,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "items", null),
                ":=",
                loopBody);

        UseStringBuilder recipe = new UseStringBuilder();
        Tree result = recipe.getVisitor().visit(loop, null);

        assertNotSame(loop, result);
        RangeStmt resultLoop = (RangeStmt) result;
        AssignStmt resultAssign = (AssignStmt) resultLoop.getBody().getStmts().get(0);

        assertTrue(resultAssign.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
