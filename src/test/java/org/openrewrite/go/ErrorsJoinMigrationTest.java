package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ErrorsJoinMigrationTest {

    @Test
    void updatesMultierrorAppendToErrorsJoin() {
        Ident multiIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "multierror", null);
        Ident appendIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Append", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, multiIdent, appendIdent, null);

        Ident arg1 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err1", null);
        Ident arg2 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err2", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(arg1, arg2), false, null);

        ErrorsJoinMigration recipe = new ErrorsJoinMigration();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resultCall = (CallExpr) result;
        assertTrue(resultCall.getFun() instanceof SelectorExpr);
        SelectorExpr resultSel = (SelectorExpr) resultCall.getFun();
        assertEquals("errors", ((Ident) resultSel.getX()).getName());
        assertEquals("Join", resultSel.getSel().getName());
    }
}
