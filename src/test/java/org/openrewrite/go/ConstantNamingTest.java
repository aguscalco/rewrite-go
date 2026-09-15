package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConstantNamingTest {

    @Test
    void renamesScreamingSnakeCaseToCamelCase() {
        Ident name = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "MAX_LIMIT_SIZE", null);
        Ident val = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "100", null);
        
        ValueSpec spec = new ValueSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(name), null, Collections.singletonList(val));
        
        GenDecl decl = new GenDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "CONST", Collections.singletonList(spec), false, Space.EMPTY);

        ConstantNaming recipe = new ConstantNaming();
        Tree result = recipe.getVisitor().visit(decl, null);

        assertNotSame(decl, result);
        GenDecl resultDecl = (GenDecl) result;
        ValueSpec resultSpec = (ValueSpec) resultDecl.getSpecs().get(0);
        assertEquals("MaxLimitSize", resultSpec.getNames().get(0).getName());
    }
}
