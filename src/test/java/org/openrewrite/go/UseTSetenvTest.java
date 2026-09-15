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

class UseTSetenvTest {

    @Test
    void migratesOsSetenvToTSetenv() {
        // Build func TestX(t *testing.T) { os.Setenv("K", "V") }
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestSomething", null);
        
        Ident testingPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tType = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
        StarExpr ptrType = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), ptrType, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        
        Ident osPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident setenvMethod = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Setenv", null);
        SelectorExpr osSetenvSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osPkg, setenvMethod, null);
        
        BasicLit key = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"KEY\"");
        BasicLit val = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "STRING", "\"VALUE\"");
        CallExpr osSetenvCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osSetenvSel, Arrays.asList(key, val), false, null);
        ExprStmt osSetenvStmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osSetenvCall);
        
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(osSetenvStmt), Space.EMPTY);
        
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        UseTSetenv recipe = new UseTSetenv();
        Tree result = recipe.getVisitor().visit(funcDecl, null);

        assertNotSame(funcDecl, result);
        assertInstanceOf(FuncDecl.class, result);
        FuncDecl resultDecl = (FuncDecl) result;

        ExprStmt stmt = (ExprStmt) resultDecl.getBody().getStmts().get(0);
        CallExpr call = (CallExpr) stmt.getExpr();
        SelectorExpr sel = (SelectorExpr) call.getFun();
        
        assertEquals("t", ((Ident) sel.getX()).getName());
        assertEquals("Setenv", sel.getSel().getName());
    }
}
