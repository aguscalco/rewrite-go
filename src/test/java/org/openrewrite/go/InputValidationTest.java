package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InputValidationTest {

    @Test
    void addsNilCheckToExportedFunction() {
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ProcessData", null);
        
        Ident paramIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "req", null);
        Ident typeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Request", null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, typeIdent, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("req"), ptrType, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, new ArrayList<>(), Space.EMPTY);
        
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        InputValidation recipe = new InputValidation();
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertNotSame(funcDecl, result);
        assertInstanceOf(FuncDecl.class, result);
        FuncDecl resultDecl = (FuncDecl) result;

        assertEquals(1, resultDecl.getBody().getStmts().size());
        assertInstanceOf(IfStmt.class, resultDecl.getBody().getStmts().get(0));
        
        IfStmt ifStmt = (IfStmt) resultDecl.getBody().getStmts().get(0);
        assertInstanceOf(BinaryExpr.class, ifStmt.getCond());
        
        BinaryExpr cond = (BinaryExpr) ifStmt.getCond();
        assertEquals("==", cond.getOp());
    }
}
