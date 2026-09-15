package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddTestHelperTest {

    @Test
    void addsHelperToTestHelperFunction() {
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "assertSomething", null);
        
        Ident testingPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), ptrType, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, new ArrayList<>(), Space.EMPTY);
        
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        AddTestHelper recipe = new AddTestHelper();
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertNotSame(funcDecl, result);
        assertInstanceOf(FuncDecl.class, result);
        FuncDecl resultDecl = (FuncDecl) result;

        assertEquals(1, resultDecl.getBody().getStmts().size());
        ExprStmt stmt = (ExprStmt) resultDecl.getBody().getStmts().get(0);
        CallExpr call = (CallExpr) stmt.getExpr();
        SelectorExpr sel = (SelectorExpr) call.getFun();
        
        assertEquals("t", ((Ident) sel.getX()).getName());
        assertEquals("Helper", sel.getSel().getName());
    }

    @Test
    void ignoresTestFunctions() {
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestSomething", null);
        
        Ident testingPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), ptrType, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, new ArrayList<>(), Space.EMPTY);
        
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        AddTestHelper recipe = new AddTestHelper();
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertSame(funcDecl, result);
    }
}
