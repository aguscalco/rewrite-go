package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RemoveRedundantTypeTest {

    @Test
    void removesInnerType() {
        Ident innerType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        CompositeLit inner = new CompositeLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, innerType, Collections.emptyList(), null);

        SliceTypeExpr outerType = new SliceTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, innerType);
        CompositeLit outer = new CompositeLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, outerType, Collections.singletonList(inner), null);

        RemoveRedundantType recipe = new RemoveRedundantType();
        Tree result = recipe.getVisitor().visit(outer, null);

        assertNotSame(outer, result);
        CompositeLit resultOuter = (CompositeLit) result;
        CompositeLit resultInner = (CompositeLit) resultOuter.getElts().get(0);
        assertNull(resultInner.getType());
    }
}
