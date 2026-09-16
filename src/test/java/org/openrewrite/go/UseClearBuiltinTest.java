package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseClearBuiltinTest {

    @Test
    void updatesDeleteRangeToClear() {
        Ident key = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "k", null);
        Ident x = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null);

        Ident deleteFun = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "delete", null);
        Ident arg0 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null);
        Ident arg1 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "k", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, deleteFun, Arrays.asList(arg0, arg1), false, null);
        ExprStmt exprStmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call);

        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(exprStmt), Space.EMPTY);

        RangeStmt rangeStmt = new RangeStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, key, null, x, ":=", body);

        UseClearBuiltin recipe = new UseClearBuiltin();
        Tree result = recipe.getVisitor().visit(rangeStmt, null);

        assertNotSame(rangeStmt, result);
        assertTrue(result instanceof ExprStmt);
        ExprStmt resultStmt = (ExprStmt) result;
        assertTrue(resultStmt.getExpr() instanceof CallExpr);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        assertEquals("clear", ((Ident) resultCall.getFun()).getName());
    }
}
