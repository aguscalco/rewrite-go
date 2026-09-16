package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TempDirMigrationTest {

    @Test
    void flagsOsMkdirTempInTests() {
        Ident osIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident mkdirTempIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "MkdirTemp", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osIdent, mkdirTempIdent, null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        // Build test function params: t *testing.T
        Ident testingIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tTypeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingTSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingIdent, tTypeIdent, null);
        StarExpr starExpr = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
        Field tParam = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), starExpr, null);

        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(tParam), Collections.emptyList(), null);
        BlockStmt funcBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call)), Space.EMPTY);
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestTemp", null), null, funcType, funcBody);

        GoFile goFile = new GoFile(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, java.nio.file.Paths.get("test_test.go"), java.nio.charset.StandardCharsets.UTF_8, false, null, Collections.emptyList(), Collections.singletonList(funcDecl), Space.EMPTY, null, null);

        TempDirMigration recipe = new TempDirMigration();
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
        
        GoFile resFile = (GoFile) result;
        FuncDecl resDecl = (FuncDecl) resFile.getDeclarations().get(0);
        ExprStmt resStmt = (ExprStmt) resDecl.getBody().getStmts().get(0);
        assertTrue(resStmt.getExpr().getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
