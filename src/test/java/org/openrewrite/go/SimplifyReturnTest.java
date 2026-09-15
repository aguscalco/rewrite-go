package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimplifyReturnTest {

    @Test
    void simplifiesIfElseTrueFalse() {
        Ident trueIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "true", null);
        ReturnStmt retTrue = new ReturnStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(trueIdent));
        BlockStmt ifBlock = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(retTrue), Space.EMPTY);
        
        Ident falseIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "false", null);
        ReturnStmt retFalse = new ReturnStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(falseIdent));
        BlockStmt elseBlock = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(retFalse), Space.EMPTY);
        
        Ident cond = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "isValid", null);
        
        IfStmt ifStmt = new IfStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, cond, ifBlock, elseBlock);
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(ifStmt), Space.EMPTY);

        SimplifyReturn recipe = new SimplifyReturn();
        Tree result = recipe.getVisitor().visit(body, null);

        assertNotSame(body, result);
        BlockStmt resultBody = (BlockStmt) result;
        
        assertInstanceOf(ReturnStmt.class, resultBody.getStmts().get(0));
        ReturnStmt newRet = (ReturnStmt) resultBody.getStmts().get(0);
        assertEquals("isValid", ((Ident) newRet.getResults().get(0)).getName());
    }
}
