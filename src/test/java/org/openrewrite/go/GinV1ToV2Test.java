package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GinV1ToV2Test {

    @Test
    void updatesGinImport() {
        BasicLit path = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"gopkg.in/gin-gonic/gin.v1\"");
        ImportSpec spec = new ImportSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, path);

        GinV1ToV2 recipe = new GinV1ToV2();
        Tree result = recipe.getVisitor().visit(spec, null);

        assertNotSame(spec, result);
        ImportSpec resultSpec = (ImportSpec) result;
        assertEquals("\"github.com/gin-gonic/gin\"", resultSpec.getPath().getValue());
    }
}
