package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkConversionTest {

    @Test
    void convertsTestToBenchmark() {
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestProcess", null);
        
        Ident testingPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), ptrType, null);
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY);
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        BenchmarkConversion recipe = new BenchmarkConversion("TestProcess");
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertNotSame(funcDecl, result);
        assertInstanceOf(FuncDecl.class, result);
        FuncDecl resultDecl = (FuncDecl) result;

        assertEquals("BenchmarkProcess", resultDecl.getName().getName());
        
        Field newParam = resultDecl.getType().getParams().get(0);
        SelectorExpr newSel = (SelectorExpr) ((StarExpr) newParam.getType()).getX();
        assertEquals("B", newSel.getSel().getName());
        
        assertEquals(1, resultDecl.getBody().getStmts().size());
        assertInstanceOf(ForStmt.class, resultDecl.getBody().getStmts().get(0));
        ForStmt forStmt = (ForStmt) resultDecl.getBody().getStmts().get(0);
        
        assertInstanceOf(AssignStmt.class, forStmt.getInit());
        assertInstanceOf(BinaryExpr.class, forStmt.getCond());
        assertInstanceOf(IncDecStmt.class, forStmt.getPost());
    }
}
