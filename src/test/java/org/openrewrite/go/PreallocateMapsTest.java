package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreallocateMapsTest {

    @Test
    void preallocatesMap() {
        // m := make(map[int]int)
        Ident mIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null);
        Ident makeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "make", null);
        Ident intIdent1 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        Ident intIdent2 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        MapTypeExpr mapType = new MapTypeExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, intIdent1, intIdent2);
        CallExpr makeCall = new CallExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, makeIdent, Collections.singletonList(mapType), false, null);
        AssignStmt mAssign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(mIdent), ":=", Collections.singletonList(makeCall));

        // for _, v := range items { m[v] = 1 }
        Ident itemsIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "items", null);
        Ident vIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "v", null);
        
        IndexExpr indexExpr = new IndexExpr(UUID.randomUUID(), Space.build("\n\t"), Markers.EMPTY, mIdent, vIdent);
        BasicLit one = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "1");
        AssignStmt mapAssign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(indexExpr), "=", Collections.singletonList(one));
        
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(mapAssign), Space.build("\n"));
        
        RangeStmt loop = new RangeStmt(UUID.randomUUID(), Space.build("\n"), Markers.EMPTY, 
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "_", null),
                vIdent,
                itemsIdent,
                ":=",
                loopBody);
                
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(mAssign, loop), Space.EMPTY);

        PreallocateMaps recipe = new PreallocateMaps();
        Tree result = recipe.getVisitor().visit(body, null);

        assertNotSame(body, result);
        assertInstanceOf(BlockStmt.class, result);
        BlockStmt resultBlock = (BlockStmt) result;

        assertInstanceOf(AssignStmt.class, resultBlock.getStmts().get(0));
        AssignStmt makeStmt = (AssignStmt) resultBlock.getStmts().get(0);
        
        CallExpr newMakeCall = (CallExpr) makeStmt.getRhs().get(0);
        assertEquals(2, newMakeCall.getArgs().size());
        
        assertInstanceOf(MapTypeExpr.class, newMakeCall.getArgs().get(0));
        assertInstanceOf(CallExpr.class, newMakeCall.getArgs().get(1)); // len(items)
        CallExpr lenCall = (CallExpr) newMakeCall.getArgs().get(1);
        assertEquals("len", ((Ident) lenCall.getFun()).getName());
        assertEquals("items", ((Ident) lenCall.getArgs().get(0)).getName());
    }
}
