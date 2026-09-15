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

class TableDrivenTestsTest {

    @Test
    void flagsConsecutiveTRuns() {
        Ident tIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        Ident runIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Run", null);
        SelectorExpr runSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tIdent, runIdent, null);
        
        CallExpr runCall1 = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, runSel, Collections.emptyList(), false, null);
        ExprStmt runStmt1 = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, runCall1);
        
        CallExpr runCall2 = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, runSel, Collections.emptyList(), false, null);
        ExprStmt runStmt2 = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, runCall2);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(runStmt1, runStmt2), Space.EMPTY);

        TableDrivenTests recipe = new TableDrivenTests();
        Tree result = recipe.getVisitor().visit(body, null);

        assertNotSame(body, result);
        assertInstanceOf(BlockStmt.class, result);
        BlockStmt resultBlock = (BlockStmt) result;

        assertTrue(resultBlock.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
