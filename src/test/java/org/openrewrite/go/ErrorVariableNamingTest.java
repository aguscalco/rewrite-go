package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ErrorVariableNamingTest {

    @Test
    void renamesErrToErr() {
        Ident name = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "errNotFound", null);
        Ident errType = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "error", null);
        
        ValueSpec spec = new ValueSpec(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(name), errType, null);
        
        GenDecl decl = new GenDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "VAR", Collections.singletonList(spec), false, Space.EMPTY);

        ErrorVariableNaming recipe = new ErrorVariableNaming();
        Tree result = recipe.getVisitor().visit(decl, null);

        assertNotSame(decl, result);
        GenDecl resultDecl = (GenDecl) result;
        ValueSpec resultSpec = (ValueSpec) resultDecl.getSpecs().get(0);
        assertEquals("ErrNotFound", resultSpec.getNames().get(0).getName());
    }
}
