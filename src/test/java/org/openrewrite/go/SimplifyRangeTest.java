package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimplifyRangeTest {

    @Test
    void removesBlankValue() {
        Ident key = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "k", null);
        Ident val = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "_", null);
        Ident x = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null);

        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY);
        RangeStmt rangeStmt = new RangeStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, key, val, x, ":=", body);

        SimplifyRange recipe = new SimplifyRange();
        Tree result = recipe.getVisitor().visit(rangeStmt, null);

        assertNotSame(rangeStmt, result);
        RangeStmt resRange = (RangeStmt) result;
        assertNull(resRange.getValue());
    }
}
