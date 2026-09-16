package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FmtPrintfNoArgsTest {

    @Test
    void migratesPrintfNoArgs() {
        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident printfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Printf", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, printfIdent, null);
        
        BasicLit stringLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"");
        
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.singletonList(stringLit), false, null);

        FmtPrintfNoArgs recipe = new FmtPrintfNoArgs();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        SelectorExpr resSel = (SelectorExpr) resCall.getFun();
        assertEquals("Print", resSel.getSel().getName());
    }
}
