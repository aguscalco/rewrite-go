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

class DetectContextLeakTest {

    @Test
    void flagsMissingDeferCancel() {
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withCancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithCancel", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ctxIdent, withCancelIdent, null);

        Ident arg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(arg), false, null);

        Ident retCtx = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancel = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(retCtx, cancel), ":=", Collections.singletonList(call));

        BlockStmt block = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(assign), Space.EMPTY);

        DetectContextLeak recipe = new DetectContextLeak();
        Tree result = recipe.getVisitor().visit(block, null);

        assertNotSame(block, result);
        BlockStmt resultBlock = (BlockStmt) result;
        assertTrue(resultBlock.getStmts().get(0).getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
