package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MutexByValueTest {

    @Test
    void convertsMutexToStarMutex() {
        Ident syncIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sync", null);
        Ident mutexIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Mutex", null);
        SelectorExpr type = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, syncIdent, mutexIdent, null);

        Field field = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("mu"), type, null);

        MutexByValue recipe = new MutexByValue();
        Tree result = recipe.getVisitor().visit(field, null);

        assertNotSame(field, result);
        Field resultField = (Field) result;
        assertTrue(resultField.getType() instanceof StarExpr);
        StarExpr star = (StarExpr) resultField.getType();
        assertTrue(star.getX() instanceof SelectorExpr);
    }
}
