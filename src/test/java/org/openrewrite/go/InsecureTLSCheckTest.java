package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InsecureTLSCheckTest {

    @Test
    void disablesInsecureSkipVerify() {
        Ident key = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "InsecureSkipVerify", null);
        Ident val = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "true", null);
        KeyValueExpr kv = new KeyValueExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, key, val);

        InsecureTLSCheck recipe = new InsecureTLSCheck();
        Tree result = recipe.getVisitor().visit(kv, null);

        assertNotSame(kv, result);
        KeyValueExpr resKv = (KeyValueExpr) result;
        assertEquals("false", ((Ident) resKv.getValue()).getName());
    }
}
