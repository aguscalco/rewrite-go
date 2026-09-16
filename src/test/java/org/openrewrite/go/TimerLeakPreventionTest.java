package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Cursor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimerLeakPreventionTest {

    @Test
    void flagsTimeAfterInLoopSelect() {
        Ident timeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "time", null);
        Ident afterIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "After", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, timeIdent, afterIdent, null);

        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Collections.emptyList(), false, null);

        ForStmt forStmt = new ForStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, null, null, new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call)), Space.EMPTY));

        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "test", null);
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), null);
        BlockStmt funcBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(forStmt), Space.EMPTY);
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, funcBody);
        
        GoFile goFile = new GoFile(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, java.nio.file.Paths.get("test.go"), java.nio.charset.StandardCharsets.UTF_8, false, null, Collections.emptyList(), Collections.singletonList(funcDecl), Space.EMPTY, null, null);

        TimerLeakPrevention recipe = new TimerLeakPrevention();
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
    }
}
