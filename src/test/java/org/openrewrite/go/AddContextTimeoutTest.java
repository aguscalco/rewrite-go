package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddContextTimeoutTest {

    @Test
    void addsTimeoutToContextBackground() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
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
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(ctxIdent),
            ":=",
            Collections.singletonList(backgroundCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        recipe.timeoutDuration = "30 * time.Second";
        recipe.variableName = "ctx";
        
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        assertInstanceOf(AssignStmt.class, result);
        AssignStmt resultAssign = (AssignStmt) result;
        
        assertEquals(2, resultAssign.getLhs().size());
        assertEquals("ctx", ((Ident) resultAssign.getLhs().get(0)).getName());
        assertEquals("cancel", ((Ident) resultAssign.getLhs().get(1)).getName());
        
        assertEquals(1, resultAssign.getRhs().size());
        assertInstanceOf(CallExpr.class, resultAssign.getRhs().get(0));
        
        CallExpr withTimeoutCall = (CallExpr) resultAssign.getRhs().get(0);
        assertInstanceOf(SelectorExpr.class, withTimeoutCall.getFun());
        SelectorExpr withTimeoutSel = (SelectorExpr) withTimeoutCall.getFun();
        assertEquals("context", ((Ident) withTimeoutSel.getX()).getName());
        assertEquals("WithTimeout", withTimeoutSel.getSel().getName());
        
        assertEquals(2, withTimeoutCall.getArgs().size());
        assertInstanceOf(CallExpr.class, withTimeoutCall.getArgs().get(0));
        assertInstanceOf(Ident.class, withTimeoutCall.getArgs().get(1));
        assertEquals("30 * time.Second", ((Ident) withTimeoutCall.getArgs().get(1)).getName());
    }

    @Test
    void addsTimeoutToContextTODO() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident todoIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TODO", null);
        SelectorExpr todoSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            todoIdent,
            null
        );
        
        CallExpr todoCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            todoSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(ctxIdent),
            ":=",
            Collections.singletonList(todoCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        recipe.timeoutDuration = "5 * time.Second";
        recipe.variableName = "ctx";
        
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        assertInstanceOf(AssignStmt.class, result);
        AssignStmt resultAssign = (AssignStmt) result;
        
        assertEquals(2, resultAssign.getLhs().size());
        assertEquals("cancel", ((Ident) resultAssign.getLhs().get(1)).getName());
        
        CallExpr withTimeoutCall = (CallExpr) resultAssign.getRhs().get(0);
        assertEquals("5 * time.Second", ((Ident) withTimeoutCall.getArgs().get(1)).getName());
    }

    @Test
    void doesNotModifyNonContextAssignment() {
        Ident otherIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "other", null);
        Ident methodIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Method", null);
        SelectorExpr methodSel = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            otherIdent,
            methodIdent,
            null
        );
        
        CallExpr methodCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            methodSel,
            Collections.emptyList(),
            false,
            null
        );
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(ctxIdent),
            ":=",
            Collections.singletonList(methodCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        assertSame(assignStmt, result);
    }

    @Test
    void doesNotModifyDifferentVariableName() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
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
        
        Ident otherCtx = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "otherCtx", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(otherCtx),
            ":=",
            Collections.singletonList(backgroundCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        recipe.variableName = "ctx";
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        assertSame(assignStmt, result);
    }

    @Test
    void doesNotModifyMultipleAssignment() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
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
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        Ident otherIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "other", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(ctxIdent, otherIdent),
            ":=",
            Collections.singletonList(backgroundCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        assertSame(assignStmt, result);
    }

    @Test
    void usesCustomTimeoutDuration() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
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
        
        Ident ctxIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ctx", null);
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(ctxIdent),
            ":=",
            Collections.singletonList(backgroundCall)
        );

        AddContextTimeout recipe = new AddContextTimeout();
        recipe.timeoutDuration = "10 * time.Minute";
        recipe.variableName = "ctx";
        
        Stmt result = (Stmt) recipe.getVisitor().visit(assignStmt, null);

        AssignStmt resultAssign = (AssignStmt) result;
        CallExpr withTimeoutCall = (CallExpr) resultAssign.getRhs().get(0);
        assertEquals("10 * time.Minute", ((Ident) withTimeoutCall.getArgs().get(1)).getName());
    }
}
