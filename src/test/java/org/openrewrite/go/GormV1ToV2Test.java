package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GormV1ToV2Test {

    @Test
    void updatesGormImport() {
        BasicLit path = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/jinzhu/gorm\"");
        ImportSpec spec = new ImportSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, path);

        GormV1ToV2 recipe = new GormV1ToV2();
        Tree result = recipe.getVisitor().visit(spec, null);

        assertNotSame(spec, result);
        ImportSpec resultSpec = (ImportSpec) result;
        assertEquals("\"gorm.io/gorm\"", resultSpec.getPath().getValue());
    }
}
