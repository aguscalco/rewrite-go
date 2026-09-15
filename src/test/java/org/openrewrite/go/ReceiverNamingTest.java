package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverNamingTest {

    @Test
    void renamesSelfToC() {
        Ident typeIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "Client", null);
        StarExpr starType = new StarExpr(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, typeIdent, null);
        Field recvField = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("self"), starType, null);
        
        Ident funcName = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "Do", null);
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, null, null);
        
        Ident selfRef = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "self", null);
        Ident idRef = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ID", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, selfRef, idRef, null);
        ExprStmt stmt = new ExprStmt(UUID.randomUUID(), Space.build("\n\t"), Markers.EMPTY, sel);
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.singletonList(stmt), Space.build("\n"));
        
        FuncDecl func = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, recvField, funcName, null, funcType, body);

        ReceiverNaming recipe = new ReceiverNaming();
        Tree result = recipe.getVisitor().visit(func, null);

        assertNotSame(func, result);
        FuncDecl resultFunc = (FuncDecl) result;
        
        // Receiver should be 'c'
        String resultRecv = resultFunc.getRecv().getNames().get(0);
        assertEquals("c", resultRecv);
        
        // Body reference should be 'c'
        ExprStmt resultStmt = (ExprStmt) resultFunc.getBody().getStmts().get(0);
        SelectorExpr resultSel = (SelectorExpr) resultStmt.getExpr();
        assertEquals("c", ((Ident) resultSel.getX()).getName());
    }
}
