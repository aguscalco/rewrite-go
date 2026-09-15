package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PackageCommentTest {

    @Test
    void addsPackageComment() {
        Ident pkgIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "utils", null);
        PackageClause pkgClause = new PackageClause(UUID.randomUUID(), Space.build("\n\n"), Markers.EMPTY, pkgIdent);
        GoFile file = new GoFile(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Paths.get("utils.go"), null, false, pkgClause, Collections.emptyList(), null, Space.EMPTY, null, null);

        PackageComment recipe = new PackageComment();
        Tree result = recipe.getVisitor().visit(file, null);

        assertNotSame(file, result);
        GoFile resultFile = (GoFile) result;
        assertTrue(resultFile.getPackageClause().getPrefix().getWhitespace().contains("// Package utils is undocumented."));
        assertEquals("\n\n// Package utils is undocumented.\n", resultFile.getPackageClause().getPrefix().getWhitespace());
    }
}
