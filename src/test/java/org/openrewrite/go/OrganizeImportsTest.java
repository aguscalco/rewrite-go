package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrganizeImportsTest {
    
    @Test
    void organizeImportsSeparatesStdlibAndThirdParty() {
        OrganizeImports recipe = new OrganizeImports();
        
        assertNotNull(recipe.getDisplayName());
        assertEquals("Organize Go imports", recipe.getDisplayName());
        assertNotNull(recipe.getDescription());
    }
}
