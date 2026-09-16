package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.Cursor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeferInLoopTest {

    @Test
    void flagsDeferInLoop() {
        Ident closeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "close", null);
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, closeIdent, Collections.emptyList(), false, null);
        DeferStmt deferStmt = new DeferStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, call);

        BlockStmt block = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(deferStmt), Space.EMPTY);
        ForStmt forStmt = new ForStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, null, null, block);

        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "test", null);
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), null);
        BlockStmt funcBody = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(forStmt), Space.EMPTY);
        FuncDecl funcDecl = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, funcBody);

        GoFile goFile = new GoFile(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, java.nio.file.Paths.get("test.go"), java.nio.charset.StandardCharsets.UTF_8, false, null, Collections.emptyList(), Collections.singletonList(funcDecl), Space.EMPTY, null, null);

        DeferInLoop recipe = new DeferInLoop();
        Tree result = recipe.getVisitor().visit(goFile, null);

        assertNotSame(goFile, result);
    }
}
