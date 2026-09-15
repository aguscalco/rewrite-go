package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OptimizeStringConversionTest {

    @Test
    void optimizesWrite() {
        // w.Write([]byte(s))
        Ident wIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "w", null);
        Ident writeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Write", null);
        SelectorExpr wWrite = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, wIdent, writeIdent, null);
        
        Ident byteIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "byte", null);
        SliceTypeExpr sliceByte = new SliceTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, byteIdent);
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        CallExpr cast = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sliceByte, Collections.singletonList(sIdent), false, null);
        
        CallExpr writeCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, wWrite, Collections.singletonList(cast), false, null);

        OptimizeStringConversion recipe = new OptimizeStringConversion();
        Tree result = recipe.getVisitor().visit(writeCall, null);

        assertNotSame(writeCall, result);
        assertInstanceOf(CallExpr.class, result);
        CallExpr resultCall = (CallExpr) result;
        
        // io.WriteString
        assertInstanceOf(SelectorExpr.class, resultCall.getFun());
        SelectorExpr fun = (SelectorExpr) resultCall.getFun();
        assertEquals("io", ((Ident) fun.getX()).getName());
        assertEquals("WriteString", fun.getSel().getName());
        
        // (w, s)
        assertEquals(2, resultCall.getArgs().size());
        assertEquals("w", ((Ident) resultCall.getArgs().get(0)).getName());
        assertEquals("s", ((Ident) resultCall.getArgs().get(1)).getName());
    }
}
