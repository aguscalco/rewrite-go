package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExampleTestGenerationTest {

    @Test
    void generatesExample() {
        GoFile goFile = new GoFile(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Paths.get("some_test.go"),
                java.nio.charset.StandardCharsets.UTF_8,
                false,
                new PackageClause(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "test", null)),
                Collections.emptyList(),
                Collections.emptyList(),
                Space.EMPTY,
                null,
                null
        );

        ExampleTestGeneration recipe = new ExampleTestGeneration("Process");
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
        assertInstanceOf(GoFile.class, result);
        GoFile resultFile = (GoFile) result;

        assertEquals(1, resultFile.getDeclarations().size());
        assertInstanceOf(FuncDecl.class, resultFile.getDeclarations().get(0));
        FuncDecl exampleDecl = (FuncDecl) resultFile.getDeclarations().get(0);
        
        assertEquals("ExampleProcess", exampleDecl.getName().getName());
        assertEquals(0, exampleDecl.getBody().getStmts().size());
    }
}
