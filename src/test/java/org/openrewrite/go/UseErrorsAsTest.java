package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseErrorsAsTest {
    
    @Test
    void convertTypeAssertionToErrorsAs() {
        // Create: myErr, ok := err.(*MyError)
        TypeAssertExpr typeAssert = new TypeAssertExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null),
            new StarExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "MyError", null),
                null
            ),
            null
        );
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "myErr", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "ok", null)
            ),
            ":=",
            Collections.singletonList(typeAssert)
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null),
            Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            body
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        UseErrorsAs recipe = new UseErrorsAs();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        AssignStmt resultAssign = (AssignStmt) resultBody.getStmts().get(0);
        
        // Should be: myErr, ok := errors.As(err, &myErr)
        CallExpr resultCall = (CallExpr) resultAssign.getRhs().get(0);
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("errors", pkgIdent.getName());
        assertEquals("As", resultSelector.getSel().getName());
        
        assertEquals(2, resultCall.getArgs().size());
        Ident errArg = (Ident) resultCall.getArgs().get(0);
        assertEquals("err", errArg.getName());
        
        UnaryExpr addrArg = (UnaryExpr) resultCall.getArgs().get(1);
        assertEquals("&", addrArg.getOp());
        Ident targetArg = (Ident) addrArg.getX();
        assertEquals("myErr", targetArg.getName());
    }
    
    @Test
    void doNotConvertNonErrorTypeAssertions() {
        // Create: x, ok := val.(int)
        TypeAssertExpr typeAssert = new TypeAssertExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "val", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
            null
        );
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "ok", null)
            ),
            ":=",
            Collections.singletonList(typeAssert)
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(assignStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null),
            Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            body
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        UseErrorsAs recipe = new UseErrorsAs();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        AssignStmt resultAssign = (AssignStmt) resultBody.getStmts().get(0);
        
        // Should remain unchanged
        TypeAssertExpr resultTypeAssert = (TypeAssertExpr) resultAssign.getRhs().get(0);
        Ident valIdent = (Ident) resultTypeAssert.getX();
        assertEquals("val", valIdent.getName());
    }
}
