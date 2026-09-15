package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExportedCommentTest {

    @Test
    void addsFuncComment() {
        Ident name = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "DoSomething", null);
        FuncType type = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, null, null);
        FuncDecl func = new FuncDecl(UUID.randomUUID(), Space.build("\n\n"), Markers.EMPTY, null, name, null, type, null);

        ExportedComment recipe = new ExportedComment();
        Tree result = recipe.getVisitor().visit(func, null);

        assertNotSame(func, result);
        FuncDecl resultFunc = (FuncDecl) result;
        assertTrue(resultFunc.getPrefix().getWhitespace().contains("// DoSomething is undocumented."));
    }
}
