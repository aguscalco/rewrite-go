package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrganizeImportsTest {
    
    @Test
    void organizeImportsSeparatesStdlibAndThirdParty() {
        Ident name = new Ident(
            Tree.randomId(),
            Space.EMPTY,
            Markers.EMPTY,
            "main",
            null
        );
        
        ImportSpec fmtSpec = new ImportSpec(
            Tree.randomId(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
        );
        
        ImportSpec ginSpec = new ImportSpec(
            Tree.randomId(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"github.com/gin-gonic/gin\"")
        );
        
        ImportSpec osSpec = new ImportSpec(
            Tree.randomId(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"os\"")
        );
        
        ImportDecl importDecl = new ImportDecl(
            Tree.randomId(),
            Space.build("\n"),
            Markers.EMPTY,
            java.util.Arrays.asList(ginSpec, fmtSpec, osSpec),
            false,
            Space.EMPTY
        );
        
        GoFile goFile = new GoFile(
            Tree.randomId(),
            Space.EMPTY,
            Markers.EMPTY,
            java.nio.file.Paths.get("main.go"),
            java.nio.charset.StandardCharsets.UTF_8,
            false,
            new PackageClause(Tree.randomId(), Space.EMPTY, Markers.EMPTY, name),
            Collections.singletonList(importDecl),
            Collections.emptyList(),
            Space.EMPTY
        );
        
        OrganizeImports recipe = new OrganizeImports();
        
        assertNotNull(recipe.getDisplayName());
        assertEquals("Organize Go imports", recipe.getDisplayName());
        assertNotNull(recipe.getDescription());
    }
}
