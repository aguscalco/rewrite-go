package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GinBindValidationTest {

    @Test
    void migratesBindJSON() {
        Ident cIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "c", null);
        Ident bindIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "BindJSON", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, cIdent, bindIdent, null);
        
        Ident reqIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "req", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(reqIdent), false, null);

        GinBindValidation recipe = new GinBindValidation();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("ShouldBindJSON", resSel.getSel().getName());
    }
}
