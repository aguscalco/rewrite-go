package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RedundantBoolCmpTest {

    @Test
    void migratesBoolCmpTrue() {
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident trueIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "true", null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bIdent, "==", trueIdent, null);

        RedundantBoolCmp recipe = new RedundantBoolCmp();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof Ident);
        assertEquals("b", ((Ident) result).getName());
    }

    @Test
    void migratesBoolCmpFalse() {
        Ident bIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "b", null);
        Ident falseIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "false", null);
        
        BinaryExpr binary = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, bIdent, "==", falseIdent, null);

        RedundantBoolCmp recipe = new RedundantBoolCmp();
        Tree result = recipe.getVisitor().visit(binary, null);

        assertNotSame(binary, result);
        assertTrue(result instanceof UnaryExpr);
        UnaryExpr un = (UnaryExpr) result;
        assertEquals("!", un.getOp());
        assertEquals("b", ((Ident) un.getX()).getName());
    }
}
