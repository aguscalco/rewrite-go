package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseSyncPoolTest {

    @Test
    void flagsAllocation() {
        Ident vIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "v", null);
        
        Ident structIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "MyStruct", null);
        CompositeLit structLit = new CompositeLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, structIdent, Collections.emptyList(), null);
        UnaryExpr alloc = new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "&", structLit, null);
        
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(vIdent), ":=", Collections.singletonList(alloc));
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(assign), Space.build("\n"));
        
        ForStmt loop = new ForStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, 
                null, null, null, loopBody);

        UseSyncPool recipe = new UseSyncPool();
        Tree result = recipe.getVisitor().visit(loop, null);

        assertNotSame(loop, result);
        ForStmt resultLoop = (ForStmt) result;
        AssignStmt resultAssign = (AssignStmt) resultLoop.getBody().getStmts().get(0);

        assertTrue(resultAssign.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
