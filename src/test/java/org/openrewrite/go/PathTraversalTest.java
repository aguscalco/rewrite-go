package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PathTraversalTest {

    @Test
    void wrapsOsOpenArg() {
        Ident osPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident openMethod = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Open", null);
        SelectorExpr openSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osPkg, openMethod, null);

        Ident fileArg = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "userInput", null);
        
        CallExpr openCall = new CallExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                openSel,
                Collections.singletonList(fileArg),
                false,
                null
        );

        PathTraversal recipe = new PathTraversal();
        Tree result = recipe.getVisitor().visit(openCall, null);

        assertNotSame(openCall, result);
        assertInstanceOf(CallExpr.class, result);
        CallExpr resultCall = (CallExpr) result;

        Expr arg0 = resultCall.getArgs().get(0);
        assertInstanceOf(CallExpr.class, arg0);
        CallExpr cleanCall = (CallExpr) arg0;
        
        assertInstanceOf(SelectorExpr.class, cleanCall.getFun());
        SelectorExpr cleanSel = (SelectorExpr) cleanCall.getFun();
        assertEquals("filepath", ((Ident) cleanSel.getX()).getName());
        assertEquals("Clean", cleanSel.getSel().getName());
        
        Expr innerArg = cleanCall.getArgs().get(0);
        assertInstanceOf(Ident.class, innerArg);
        assertEquals("userInput", ((Ident) innerArg).getName());
        assertEquals("", innerArg.getPrefix().getWhitespace());
        assertEquals(" ", cleanCall.getPrefix().getWhitespace());
    }
}
