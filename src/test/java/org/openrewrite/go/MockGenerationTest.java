package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockGenerationTest {

    @Test
    void generatesMock() {
        GoFile goFile = new GoFile(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Paths.get("some.go"),
                java.nio.charset.StandardCharsets.UTF_8,
                false,
                new PackageClause(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "service", null)),
                Collections.emptyList(),
                Collections.emptyList(),
                Space.EMPTY,
                null,
                null
        );

        MockGeneration recipe = new MockGeneration("Database");
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
        assertInstanceOf(GoFile.class, result);
        GoFile resultFile = (GoFile) result;

        assertEquals(1, resultFile.getDeclarations().size());
        assertInstanceOf(GenDecl.class, resultFile.getDeclarations().get(0));
        GenDecl mockDecl = (GenDecl) resultFile.getDeclarations().get(0);
        
        assertEquals("type", mockDecl.getTok());
        assertEquals(1, mockDecl.getSpecs().size());
        
        TypeSpec typeSpec = (TypeSpec) mockDecl.getSpecs().get(0);
        assertEquals("MockDatabase", typeSpec.getName().getName());
        assertInstanceOf(StructTypeExpr.class, typeSpec.getType());
    }
}
