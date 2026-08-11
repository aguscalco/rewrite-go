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

class OrganizeImportsTest {
    
    @Test
    void organizeImportsSeparatesStdlibAndThirdParty() {
        // Create imports: github.com/gin-gonic/gin, fmt, os
        ImportSpec ginSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/gin-gonic/gin\"")
        );
        
        ImportSpec fmtSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
        );
        
        ImportSpec osSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"os\"")
        );
        
        ImportDecl importDecl = new ImportDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            Arrays.asList(ginSpec, fmtSpec, osSpec),
            true,
            Space.EMPTY
        );
        
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
            Collections.singletonList(importDecl),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        OrganizeImports recipe = new OrganizeImports();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertNotNull(result);
        assertTrue(result instanceof GoFile);
        
        GoFile resultFile = (GoFile) result;
        
        // Should have 2 import declarations: one for stdlib, one for third-party
        assertEquals(2, resultFile.getImports().size());
        
        // First import should be stdlib (fmt, os)
        ImportDecl stdlibImport = resultFile.getImports().get(0);
        assertEquals(2, stdlibImport.getSpecs().size());
        assertEquals("\"fmt\"", stdlibImport.getSpecs().get(0).getPath().getValue());
        assertEquals("\"os\"", stdlibImport.getSpecs().get(1).getPath().getValue());
        
        // Second import should be third-party (gin)
        ImportDecl thirdPartyImport = resultFile.getImports().get(1);
        assertEquals(1, thirdPartyImport.getSpecs().size());
        assertEquals("\"github.com/gin-gonic/gin\"", thirdPartyImport.getSpecs().get(0).getPath().getValue());
    }
    
    @Test
    void organizeImportsWithOnlyStdlib() {
        ImportSpec fmtSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
        );
        
        ImportSpec osSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"os\"")
        );
        
        ImportDecl importDecl = new ImportDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            Arrays.asList(osSpec, fmtSpec),
            true,
            Space.EMPTY
        );
        
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
            Collections.singletonList(importDecl),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        OrganizeImports recipe = new OrganizeImports();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        
        // Should have 1 import declaration (stdlib only)
        assertEquals(1, resultFile.getImports().size());
        
        // Should be sorted alphabetically
        ImportDecl stdlibImport = resultFile.getImports().get(0);
        assertEquals(2, stdlibImport.getSpecs().size());
        assertEquals("\"fmt\"", stdlibImport.getSpecs().get(0).getPath().getValue());
        assertEquals("\"os\"", stdlibImport.getSpecs().get(1).getPath().getValue());
    }
    
    @Test
    void organizeImportsWithOnlyThirdParty() {
        ImportSpec ginSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/gin-gonic/gin\"")
        );
        
        ImportSpec echoSpec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/labstack/echo\"")
        );
        
        ImportDecl importDecl = new ImportDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            Arrays.asList(echoSpec, ginSpec),
            true,
            Space.EMPTY
        );
        
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
            Collections.singletonList(importDecl),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        OrganizeImports recipe = new OrganizeImports();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        
        // Should have 1 import declaration (third-party only)
        assertEquals(1, resultFile.getImports().size());
        
        // Should be sorted alphabetically
        ImportDecl thirdPartyImport = resultFile.getImports().get(0);
        assertEquals(2, thirdPartyImport.getSpecs().size());
        assertEquals("\"github.com/gin-gonic/gin\"", thirdPartyImport.getSpecs().get(0).getPath().getValue());
        assertEquals("\"github.com/labstack/echo\"", thirdPartyImport.getSpecs().get(1).getPath().getValue());
    }
    
    @Test
    void organizeImportsWithNoImports() {
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
            Collections.emptyList(),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        OrganizeImports recipe = new OrganizeImports();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        
        // Should have no imports
        assertEquals(0, resultFile.getImports().size());
    }
}
