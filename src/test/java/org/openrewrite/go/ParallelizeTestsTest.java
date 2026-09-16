package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParallelizeTestsTest {

    @Test
    void flagsMissingParallel() {
        Ident tIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "t", null);
        Ident runIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Run", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tIdent, runIdent, null);

        Ident nameArg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "\"subtest\"", null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), null);
        BlockStmt funcBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY);
        FuncLit funcLit = new FuncLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, funcType, funcBody);

        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(nameArg, funcLit), false, null);

        // Wrap in test func to be safe with SearchResult
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestWrapper", null), null, funcType, new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call)), Space.EMPTY));
        GoFile goFile = new GoFile(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, java.nio.file.Paths.get("test_test.go"), java.nio.charset.StandardCharsets.UTF_8, false, null, Collections.emptyList(), Collections.singletonList(funcDecl), Space.EMPTY, null, null);

        ParallelizeTests recipe = new ParallelizeTests();
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
        
        GoFile resFile = (GoFile) result;
        FuncDecl resDecl = (FuncDecl) resFile.getDeclarations().get(0);
        ExprStmt resStmt = (ExprStmt) resDecl.getBody().getStmts().get(0);
        assertTrue(resStmt.getExpr().getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
