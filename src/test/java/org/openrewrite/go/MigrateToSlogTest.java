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

class MigrateToSlogTest {
    
    @Test
    void convertLogPrintfToSlogInfo() {
        // Create: log.Printf("message: %v", value)
        SelectorExpr logSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "log", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Printf", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            logSelector,
            Arrays.asList(
                new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"message: %v\""),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "value", null)
            ),
            false,
            null
        );
        
        ExprStmt exprStmt = new ExprStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            callExpr
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(exprStmt),
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
        
        MigrateToSlog recipe = new MigrateToSlog();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("slog", pkgIdent.getName());
        assertEquals("Info", resultSelector.getSel().getName());
        
        assertEquals(2, resultCall.getArgs().size());
    }
    
    @Test
    void convertLogPrintlnToSlogInfo() {
        // Create: log.Println("message")
        SelectorExpr logSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "log", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Println", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            logSelector,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"message\"")),
            false,
            null
        );
        
        ExprStmt exprStmt = new ExprStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            callExpr
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(exprStmt),
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
        
        MigrateToSlog recipe = new MigrateToSlog();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("slog", pkgIdent.getName());
        assertEquals("Info", resultSelector.getSel().getName());
    }
    
    @Test
    void doNotConvertOtherLogMethods() {
        // Create: log.Fatal("error")
        SelectorExpr logSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "log", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Fatal", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            logSelector,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"error\"")),
            false,
            null
        );
        
        ExprStmt exprStmt = new ExprStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            callExpr
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(exprStmt),
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
        
        MigrateToSlog recipe = new MigrateToSlog();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        
        // Should remain unchanged
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("log", pkgIdent.getName());
        assertEquals("Fatal", resultSelector.getSel().getName());
    }
}
