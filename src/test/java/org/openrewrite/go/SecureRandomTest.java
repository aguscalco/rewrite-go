package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecureRandomTest {

    @Test
    void replacesMathRandWithCryptoRand() {
        BasicLit pathLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"math/rand\"");
        ImportSpec spec = new ImportSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, pathLit);
        ImportDecl decl = new ImportDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(spec), false, Space.EMPTY);
        
        SecureRandom recipe = new SecureRandom();
        Tree result = recipe.getVisitor().visit(decl, null);
        
        assertNotSame(decl, result);
        assertInstanceOf(ImportDecl.class, result);
        ImportDecl resultDecl = (ImportDecl) result;
        
        assertEquals("\"crypto/rand\"", resultDecl.getSpecs().get(0).getPath().getValue());
    }
}
