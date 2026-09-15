package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EchoV4ToV5Test {

    @Test
    void updatesEchoImport() {
        BasicLit path = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/labstack/echo/v4/middleware\"");
        ImportSpec spec = new ImportSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, path);

        EchoV4ToV5 recipe = new EchoV4ToV5();
        Tree result = recipe.getVisitor().visit(spec, null);

        assertNotSame(spec, result);
        ImportSpec resultSpec = (ImportSpec) result;
        assertEquals("\"github.com/labstack/echo/v5/middleware\"", resultSpec.getPath().getValue());
    }
}
