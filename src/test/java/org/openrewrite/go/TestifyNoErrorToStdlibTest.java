package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestifyNoErrorToStdlibTest {

    @Test
    void convertsAssertNoError() {
        Ident assertIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "assert", null);
        Ident noErrorIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "NoError", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, assertIdent, noErrorIdent, null);

        Ident tArg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        Ident errArg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(tArg, errArg), false, null);

        ExprStmt stmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call);
        BlockStmt block = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(stmt), Space.EMPTY);

        TestifyNoErrorToStdlib recipe = new TestifyNoErrorToStdlib();
        Tree result = recipe.getVisitor().visit(block, null);

        assertNotSame(block, result);
        BlockStmt resBlock = (BlockStmt) result;
        assertTrue(resBlock.getStmts().get(0) instanceof IfStmt);
        IfStmt ifStmt = (IfStmt) resBlock.getStmts().get(0);
        assertTrue(ifStmt.getCond() instanceof BinaryExpr);
        assertEquals("!=", ((BinaryExpr) ifStmt.getCond()).getOp());
    }
}
