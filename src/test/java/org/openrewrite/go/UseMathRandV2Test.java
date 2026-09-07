package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseMathRandV2Test {
    
    @Test
    void migratesMathRandImport() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(
                new ImportDecl(
                    UUID.randomUUID(),
                    Space.build("\n\n"),
                    Markers.EMPTY,
                    Collections.singletonList(
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"math/rand\"")
                        )
                    ),
                    false,
                    Space.EMPTY
                )
            ),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        UseMathRandV2 recipe = new UseMathRandV2();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertTrue(result instanceof GoFile);
        GoFile resultFile = (GoFile) result;
        
        assertEquals(1, resultFile.getImports().size());
        ImportDecl importDecl = resultFile.getImports().get(0);
        assertEquals(1, importDecl.getSpecs().size());
        ImportSpec spec = importDecl.getSpecs().get(0);
        assertEquals("\"math/rand/v2\"", spec.getPath().getValue());
    }
    
    @Test
    void preservesOtherImports() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(
                new ImportDecl(
                    UUID.randomUUID(),
                    Space.build("\n\n"),
                    Markers.EMPTY,
                    Collections.singletonList(
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
                        )
                    ),
                    false,
                    Space.EMPTY
                )
            ),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        UseMathRandV2 recipe = new UseMathRandV2();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertTrue(result instanceof GoFile);
        GoFile resultFile = (GoFile) result;
        
        assertEquals(1, resultFile.getImports().size());
        ImportDecl importDecl = resultFile.getImports().get(0);
        assertEquals(1, importDecl.getSpecs().size());
        ImportSpec spec = importDecl.getSpecs().get(0);
        assertEquals("\"fmt\"", spec.getPath().getValue());
    }
    
    @Test
    void migratesMultipleImportsIncludingMathRand() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(
                new ImportDecl(
                    UUID.randomUUID(),
                    Space.build("\n\n"),
                    Markers.EMPTY,
                    Arrays.asList(
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
                        ),
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.build("\n"),
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"math/rand\"")
                        ),
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.build("\n"),
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"time\"")
                        )
                    ),
                    true,
                    Space.EMPTY
                )
            ),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        UseMathRandV2 recipe = new UseMathRandV2();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertTrue(result instanceof GoFile);
        GoFile resultFile = (GoFile) result;
        
        assertEquals(1, resultFile.getImports().size());
        ImportDecl importDecl = resultFile.getImports().get(0);
        assertEquals(3, importDecl.getSpecs().size());
        
        assertEquals("\"fmt\"", importDecl.getSpecs().get(0).getPath().getValue());
        assertEquals("\"math/rand/v2\"", importDecl.getSpecs().get(1).getPath().getValue());
        assertEquals("\"time\"", importDecl.getSpecs().get(2).getPath().getValue());
    }
    
    @Test
    void doesNotModifyAlreadyMigratedCode() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(
                new ImportDecl(
                    UUID.randomUUID(),
                    Space.build("\n\n"),
                    Markers.EMPTY,
                    Collections.singletonList(
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            null,
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"math/rand/v2\"")
                        )
                    ),
                    false,
                    Space.EMPTY
                )
            ),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        UseMathRandV2 recipe = new UseMathRandV2();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertTrue(result instanceof GoFile);
        GoFile resultFile = (GoFile) result;
        
        assertEquals(1, resultFile.getImports().size());
        ImportDecl importDecl = resultFile.getImports().get(0);
        assertEquals(1, importDecl.getSpecs().size());
        ImportSpec spec = importDecl.getSpecs().get(0);
        assertEquals("\"math/rand/v2\"", spec.getPath().getValue());
    }
    
    @Test
    void preservesImportAlias() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(
                new ImportDecl(
                    UUID.randomUUID(),
                    Space.build("\n\n"),
                    Markers.EMPTY,
                    Collections.singletonList(
                        new ImportSpec(
                            UUID.randomUUID(),
                            Space.EMPTY,
                            Markers.EMPTY,
                            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "rand", null),
                            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"math/rand\"")
                        )
                    ),
                    false,
                    Space.EMPTY
                )
            ),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        UseMathRandV2 recipe = new UseMathRandV2();
        ExecutionContext ctx = new InMemoryExecutionContext();
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertTrue(result instanceof GoFile);
        GoFile resultFile = (GoFile) result;
        
        assertEquals(1, resultFile.getImports().size());
        ImportDecl importDecl = resultFile.getImports().get(0);
        assertEquals(1, importDecl.getSpecs().size());
        ImportSpec spec = importDecl.getSpecs().get(0);
        assertEquals("\"math/rand/v2\"", spec.getPath().getValue());
        assertEquals("rand", spec.getAlias().getName());
    }
}
