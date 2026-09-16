package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoopVarCaptureFixTest {

    @Test
    void removesVEqualsV() {
        Ident vLhs = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "v", null);
        Ident vRhs = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "v", null);
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(vLhs), ":=", Collections.singletonList(vRhs));

        ExprStmt someOtherStmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, vLhs);

        BlockStmt block = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(assign, someOtherStmt), Space.EMPTY);

        LoopVarCaptureFix recipe = new LoopVarCaptureFix();
        Tree result = recipe.getVisitor().visit(block, null);

        assertNotSame(block, result);
        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(1, resultBlock.getStmts().size());
        assertTrue(resultBlock.getStmts().get(0) instanceof ExprStmt);
    }
}
