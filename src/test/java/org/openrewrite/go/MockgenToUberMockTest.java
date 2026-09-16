package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockgenToUberMockTest {

    @Test
    void updatesImportPath() {
        BasicLit path = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/golang/mock/gomock\"");
        ImportSpec importSpec = new ImportSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, path);

        MockgenToUberMock recipe = new MockgenToUberMock();
        Tree result = recipe.getVisitor().visit(importSpec, null);

        assertNotSame(importSpec, result);
        ImportSpec resSpec = (ImportSpec) result;
        assertEquals("\"go.uber.org/mock/gomock\"", resSpec.getPath().getValue());
    }
}
