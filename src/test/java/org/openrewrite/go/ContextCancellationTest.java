package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContextCancellationTest {

    @Test
    void addsDeferCancelAfterWithTimeout() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withTimeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithTimeout", null);
        SelectorExpr withTimeoutSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withTimeoutIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident timeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "30 * time.Second", null);
        
        CallExpr withTimeoutCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withTimeoutSel,
            Arrays.asList(backgroundCall, timeoutIdent),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withTimeoutCall)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        assertInstanceOf(BlockStmt.class, result);
        BlockStmt resultBlock = (BlockStmt) result;
        
        assertEquals(2, resultBlock.getStmts().size());
        assertInstanceOf(AssignStmt.class, resultBlock.getStmts().get(0));
        assertInstanceOf(DeferStmt.class, resultBlock.getStmts().get(1));
        
        DeferStmt deferStmt = (DeferStmt) resultBlock.getStmts().get(1);
        assertInstanceOf(CallExpr.class, deferStmt.getCall());
        CallExpr cancelCall = (CallExpr) deferStmt.getCall();
        assertInstanceOf(Ident.class, cancelCall.getFun());
        assertEquals("cancel", ((Ident) cancelCall.getFun()).getName());
        assertEquals(0, cancelCall.getArgs().size());
    }

    @Test
    void addsDeferCancelAfterWithCancel() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withCancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithCancel", null);
        SelectorExpr withCancelSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withCancelIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        CallExpr withCancelCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withCancelSel,
            Collections.singletonList(backgroundCall),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withCancelCall)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(2, resultBlock.getStmts().size());
        assertInstanceOf(DeferStmt.class, resultBlock.getStmts().get(1));
    }

    @Test
    void addsDeferCancelAfterWithDeadline() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withDeadlineIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithDeadline", null);
        SelectorExpr withDeadlineSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withDeadlineIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident deadlineIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "deadline", null);
        
        CallExpr withDeadlineCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withDeadlineSel,
            Arrays.asList(backgroundCall, deadlineIdent),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withDeadlineCall)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(2, resultBlock.getStmts().size());
        assertInstanceOf(DeferStmt.class, resultBlock.getStmts().get(1));
    }

    @Test
    void doesNotAddDeferCancelIfAlreadyExists() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withTimeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithTimeout", null);
        SelectorExpr withTimeoutSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withTimeoutIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident timeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "30 * time.Second", null);
        
        CallExpr withTimeoutCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withTimeoutSel,
            Arrays.asList(backgroundCall, timeoutIdent),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withTimeoutCall)
        );
        
        Ident cancelIdent2 = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        CallExpr cancelCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            cancelIdent2,
            Collections.emptyList(),
            false,
            null
        );
        
        DeferStmt deferStmt = new DeferStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            cancelCall
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(assignStmt, deferStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(2, resultBlock.getStmts().size());
        assertSame(blockStmt, result);
    }

    @Test
    void handlesMultipleContextCreations() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withTimeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithTimeout", null);
        SelectorExpr withTimeoutSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withTimeoutIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident timeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "30 * time.Second", null);
        
        CallExpr withTimeoutCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withTimeoutSel,
            Arrays.asList(backgroundCall, timeoutIdent),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withTimeoutCall)
        );
        
        Ident ctx2Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx2", null);
        Ident cancel2Ident = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel2", null);
        
        AssignStmt assignStmt2 = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctx2Ident, cancel2Ident),
            ":=",
            Collections.singletonList(withTimeoutCall)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(assignStmt, assignStmt2),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(3, resultBlock.getStmts().size());
        assertInstanceOf(DeferStmt.class, resultBlock.getStmts().get(1));
    }

    @Test
    void doesNotModifyNonContextAssignments() {
        Ident otherIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "other", null);
        Ident valueIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "value", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(otherIdent),
            ":=",
            Collections.singletonList(valueIdent)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "ctx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        assertSame(blockStmt, result);
    }

    @Test
    void respectsCustomContextVariableName() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident withTimeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WithTimeout", null);
        SelectorExpr withTimeoutSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            withTimeoutIdent,
            null
        );
        
        Ident backgroundIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Background", null);
        SelectorExpr backgroundSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            backgroundIdent,
            null
        );
        
        CallExpr backgroundCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            backgroundSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident timeoutIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "30 * time.Second", null);
        
        CallExpr withTimeoutCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            withTimeoutSel,
            Arrays.asList(backgroundCall, timeoutIdent),
            false,
            null
        );
        
        Ident requestCtxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "requestCtx", null);
        Ident cancelIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cancel", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(requestCtxIdent, cancelIdent),
            ":=",
            Collections.singletonList(withTimeoutCall)
        );
        
        BlockStmt blockStmt = new BlockStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );

        ContextCancellation recipe = new ContextCancellation();
        recipe.contextVarName = "requestCtx";
        
        Tree result = recipe.getVisitor().visit(blockStmt, null);

        BlockStmt resultBlock = (BlockStmt) result;
        assertEquals(2, resultBlock.getStmts().size());
        assertInstanceOf(DeferStmt.class, resultBlock.getStmts().get(1));
    }
}
