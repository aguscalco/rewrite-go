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

class UseBytesBufferTest {

    @Test
    void flagsAppend() {
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident vIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "v", null);
        
        Ident appendIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "append", null);
        CallExpr appendCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, appendIdent, Arrays.asList(bIdent, vIdent), false, null);
        
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(bIdent), "=", Collections.singletonList(appendCall));
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(assign), Space.build("\n"));
        
        ForStmt loop = new ForStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, 
                null, null, null, loopBody);

        UseBytesBuffer recipe = new UseBytesBuffer();
        Tree result = recipe.getVisitor().visit(loop, null);

        assertNotSame(loop, result);
        ForStmt resultLoop = (ForStmt) result;
        AssignStmt resultAssign = (AssignStmt) resultLoop.getBody().getStmts().get(0);

        assertTrue(resultAssign.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
