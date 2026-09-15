package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PreallocateSlicesTest {

    @Test
    void preallocatesSlice() {
        // var s []int
        Ident sIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "s", null);
        Ident intIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null);
        SliceTypeExpr sliceType = new SliceTypeExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, intIdent);
        ValueSpec valueSpec = new ValueSpec(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.singletonList(sIdent), sliceType, Collections.emptyList());
        GenDecl genDecl = new GenDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "var", Collections.singletonList(valueSpec), false, Space.EMPTY);
        DeclStmt varS = new DeclStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, genDecl);

        // for _, v := range items { s = append(s, v) }
        Ident itemsIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "items", null);
        Ident appendIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "append", null);
        Ident vIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "v", null);
        CallExpr appendCall = new CallExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, appendIdent, Arrays.asList(sIdent, vIdent), false, null);
        AssignStmt appendStmt = new AssignStmt(UUID.randomUUID(), Space.build("\n\t"), Markers.EMPTY, Collections.singletonList(sIdent), "=", Collections.singletonList(appendCall));
        BlockStmt loopBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(appendStmt), Space.build("\n"));
        
        RangeStmt loop = new RangeStmt(UUID.randomUUID(), Space.build("\n"), Markers.EMPTY, 
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "_", null),
                vIdent,
                itemsIdent,
                ":=",
                loopBody);
                
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(varS, loop), Space.EMPTY);

        PreallocateSlices recipe = new PreallocateSlices();
        Tree result = recipe.getVisitor().visit(body, null);

        assertNotSame(body, result);
        assertInstanceOf(BlockStmt.class, result);
        BlockStmt resultBlock = (BlockStmt) result;

        assertInstanceOf(AssignStmt.class, resultBlock.getStmts().get(0));
        AssignStmt makeStmt = (AssignStmt) resultBlock.getStmts().get(0);
        
        assertEquals(":=", makeStmt.getTok());
        assertEquals(1, makeStmt.getLhs().size());
        assertEquals("s", ((Ident) makeStmt.getLhs().get(0)).getName());
        
        CallExpr makeCall = (CallExpr) makeStmt.getRhs().get(0);
        assertEquals("make", ((Ident) makeCall.getFun()).getName());
        assertEquals(3, makeCall.getArgs().size());
        
        assertInstanceOf(SliceTypeExpr.class, makeCall.getArgs().get(0));
        assertInstanceOf(BasicLit.class, makeCall.getArgs().get(1));
        assertInstanceOf(CallExpr.class, makeCall.getArgs().get(2)); // len(items)
        CallExpr lenCall = (CallExpr) makeCall.getArgs().get(2);
        assertEquals("len", ((Ident) lenCall.getFun()).getName());
        assertEquals("items", ((Ident) lenCall.getArgs().get(0)).getName());
    }
}
