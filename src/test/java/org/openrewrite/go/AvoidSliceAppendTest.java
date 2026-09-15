package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AvoidSliceAppendTest {

    @Test
    void avoidsAppend() {
        // s := make([]int, 0, len(items))
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident makeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "make", null);
        Ident intIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        SliceTypeExpr sliceType = new SliceTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, intIdent);
        BasicLit zero = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0");
        Ident lenIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "len", null);
        Ident itemsIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "items", null);
        CallExpr lenCall = new CallExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, lenIdent, Collections.singletonList(itemsIdent), false, null);
        CallExpr makeCall = new CallExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, makeIdent, Arrays.asList(sliceType, zero, lenCall), false, null);
        AssignStmt makeAssign = new AssignStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(sIdent), ":=", Collections.singletonList(makeCall));

        // for i, v := range items { s = append(s, v) }
        Ident iIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "i", null);
        Ident vIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "v", null);
        Ident appendIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "append", null);
        CallExpr appendCall = new CallExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, appendIdent, Arrays.asList(sIdent, vIdent), false, null);
        AssignStmt appendAssign = new AssignStmt(UUID.randomUUID(), Space.build("\n\t"), Markers.EMPTY, Collections.singletonList(sIdent), "=", Collections.singletonList(appendCall));
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(appendAssign), Space.build("\n"));
        
        RangeStmt loop = new RangeStmt(UUID.randomUUID(), Space.build("\n"), Markers.EMPTY, 
                iIdent,
                vIdent,
                itemsIdent,
                ":=",
                loopBody);
                
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(makeAssign, loop), Space.EMPTY);

        AvoidSliceAppend recipe = new AvoidSliceAppend();
        Tree result = recipe.getVisitor().visit(body, null);

        assertNotSame(body, result);
        assertInstanceOf(BlockStmt.class, result);
        BlockStmt resultBlock = (BlockStmt) result;

        // make([]int, len(items))
        AssignStmt makeStmt = (AssignStmt) resultBlock.getStmts().get(0);
        CallExpr newMakeCall = (CallExpr) makeStmt.getRhs().get(0);
        assertEquals(2, newMakeCall.getArgs().size());
        
        // for i, v := range items { s[i] = v }
        RangeStmt resultLoop = (RangeStmt) resultBlock.getStmts().get(1);
        AssignStmt indexAssign = (AssignStmt) resultLoop.getBody().getStmts().get(0);
        assertInstanceOf(IndexExpr.class, indexAssign.getLhs().get(0));
        IndexExpr idx = (IndexExpr) indexAssign.getLhs().get(0);
        assertEquals("s", ((Ident) idx.getX()).getName());
        assertEquals("i", ((Ident) idx.getIndex()).getName());
    }
}
