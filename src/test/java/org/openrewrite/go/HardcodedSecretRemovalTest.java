package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HardcodedSecretRemovalTest {

    @Test
    void extractsHardcodedSecret() {
        Ident lhs = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "apiToken", null);
        BasicLit rhs = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"super_secret_value\"");
        AssignStmt assign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(lhs), ":=", Collections.singletonList(rhs));

        HardcodedSecretRemoval recipe = new HardcodedSecretRemoval();
        Tree result = recipe.getVisitor().visit(assign, null);

        assertNotSame(assign, result);
        AssignStmt resAssign = (AssignStmt) result;
        assertTrue(resAssign.getRhs().get(0) instanceof CallExpr);
        CallExpr call = (CallExpr) resAssign.getRhs().get(0);
        assertTrue(call.getFun() instanceof SelectorExpr);
        SelectorExpr sel = (SelectorExpr) call.getFun();
        assertEquals("os", ((Ident) sel.getX()).getName());
        assertEquals("Getenv", sel.getSel().getName());
        assertEquals("\"APITOKEN\"", ((BasicLit) call.getArgs().get(0)).getValue());
    }
}
