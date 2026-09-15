package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestSubtestsTest {

    @Test
    void wrapsLoopBodyInTRun() {
        // Build func TestX(t *testing.T)
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestSomething", null);
        
        Ident testingPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), ptrType, null);
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        // Build for _, tt := range tests { t.Error("failed") }
        Ident keyIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "_", null);
        Ident valIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "tt", null);
        Ident testsIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "tests", null);
        
        // Body: t.Error("failed")
        Ident tIdent = new Ident(UUID.randomUUID(), Space.build("\n\t\t"), Markers.EMPTY, "t", null);
        Ident errIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Error", null);
        SelectorExpr tErrorSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tIdent, errIdent, null);
        BasicLit msg = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"failed\"");
        CallExpr errCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tErrorSel, Collections.singletonList(msg), false, null);
        ExprStmt errStmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, errCall);
        BlockStmt rangeBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(errStmt), Space.build("\n\t"));
        
        RangeStmt rangeStmt = new RangeStmt(
                UUID.randomUUID(),
                Space.build("\n\t"),
                Markers.EMPTY,
                keyIdent,
                valIdent,
                testsIdent,
                ":=",
                rangeBody
        );
        
        BlockStmt funcBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(rangeStmt), Space.EMPTY);
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, funcBody);

        TestSubtests recipe = new TestSubtests();
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertNotSame(funcDecl, result);
        assertInstanceOf(FuncDecl.class, result);
        FuncDecl resultDecl = (FuncDecl) result;

        RangeStmt newRange = (RangeStmt) resultDecl.getBody().getStmts().get(0);
        assertEquals(1, newRange.getBody().getStmts().size());
        
        ExprStmt runStmt = (ExprStmt) newRange.getBody().getStmts().get(0);
        CallExpr runCall = (CallExpr) runStmt.getExpr();
        SelectorExpr runSel = (SelectorExpr) runCall.getFun();
        assertEquals("t", ((Ident) runSel.getX()).getName());
        assertEquals("Run", runSel.getSel().getName());
        
        assertEquals(2, runCall.getArgs().size());
        assertInstanceOf(SelectorExpr.class, runCall.getArgs().get(0)); // tt.name
        assertInstanceOf(FuncLit.class, runCall.getArgs().get(1)); // func(t *testing.T) { ... }
        
        FuncLit funcLit = (FuncLit) runCall.getArgs().get(1);
        assertEquals(1, funcLit.getBody().getStmts().size()); // t.Error("failed")
    }
}
