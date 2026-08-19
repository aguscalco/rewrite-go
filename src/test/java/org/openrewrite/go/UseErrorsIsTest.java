package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UseErrorsIsTest {
    
    @Test
    void convertErrEqualsEOFToErrorsIs() {
        // Create: if err == io.EOF { }
        BinaryExpr comparison = new BinaryExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null),
            "==",
            new SelectorExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "io", null),
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "EOF", null),
                null
            ),
            null
        );
        
        IfStmt ifStmt = new IfStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            comparison,
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY),
            null
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(ifStmt),
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
        
        UseErrorsIs recipe = new UseErrorsIs();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        IfStmt resultIf = (IfStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultIf.getCond();
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("errors", pkgIdent.getName());
        assertEquals("Is", resultSelector.getSel().getName());
        
        assertEquals(2, resultCall.getArgs().size());
        Ident errArg = (Ident) resultCall.getArgs().get(0);
        assertEquals("err", errArg.getName());
        
        SelectorExpr eofArg = (SelectorExpr) resultCall.getArgs().get(1);
        Ident ioIdent = (Ident) eofArg.getX();
        assertEquals("io", ioIdent.getName());
        assertEquals("EOF", eofArg.getSel().getName());
    }
    
    @Test
    void convertErrNotEqualsNilToNotErrorsIs() {
        // Create: if err != nil { }
        BinaryExpr comparison = new BinaryExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null),
            "!=",
            new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "nil", null),
            null
        );
        
        IfStmt ifStmt = new IfStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            comparison,
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY),
            null
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(ifStmt),
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
        
        UseErrorsIs recipe = new UseErrorsIs();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        IfStmt resultIf = (IfStmt) resultBody.getStmts().get(0);
        
        // For != nil, we should NOT convert (nil check is fine as-is)
        // The recipe should only convert sentinel error comparisons
        BinaryExpr resultComparison = (BinaryExpr) resultIf.getCond();
        assertEquals("!=", resultComparison.getOp());
    }
    
    @Test
    void doNotConvertNonErrorComparisons() {
        // Create: if x == 5 { }
        BinaryExpr comparison = new BinaryExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null),
            "==",
            new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "5"),
            null
        );
        
        IfStmt ifStmt = new IfStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            comparison,
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY),
            null
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(ifStmt),
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
        
        UseErrorsIs recipe = new UseErrorsIs();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        IfStmt resultIf = (IfStmt) resultBody.getStmts().get(0);
        
        // Should remain unchanged
        BinaryExpr resultComparison = (BinaryExpr) resultIf.getCond();
        assertEquals("==", resultComparison.getOp());
        Ident leftIdent = (Ident) resultComparison.getX();
        assertEquals("x", leftIdent.getName());
    }
}
