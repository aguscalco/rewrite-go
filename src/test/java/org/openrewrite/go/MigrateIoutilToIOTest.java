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

class MigrateIoutilToIOTest {
    
    @Test
    void ioutilImportBecomesIoWhenUsageMapsToIo() {
        GoFile result = runWith("ReadAll");
        assertEquals(1, result.getImports().size());
        assertEquals("\"io\"", result.getImports().get(0).getSpecs().get(0).getPath().getValue());
    }
    
    @Test
    void ioutilImportBecomesOsWhenUsageMapsToOs() {
        GoFile result = runWith("ReadFile");
        assertEquals(1, result.getImports().size());
        assertEquals("\"os\"", result.getImports().get(0).getSpecs().get(0).getPath().getValue());
    }
    
    @Test
    void ioutilImportIsLeftAloneWhenThereAreNoUsages() {
        GoFile file = fileWithIoutilImport(null);
        GoFile result = (GoFile) new MigrateIoutilToIO().getVisitor().visit(file, new InMemoryExecutionContext());
        assertEquals(1, result.getImports().size());
        assertEquals("\"io/ioutil\"", result.getImports().get(0).getSpecs().get(0).getPath().getValue());
    }
    
    private GoFile runWith(String ioutilMethod) {
        GoFile file = fileWithIoutilImport(ioutilMethod);
        return (GoFile) new MigrateIoutilToIO().getVisitor().visit(file, new InMemoryExecutionContext());
    }
    
    /** package main; import "io/ioutil"; func main() { ioutil.<method>(r) } */
    private GoFile fileWithIoutilImport(String ioutilMethod) {
        ImportDecl importDecl = new ImportDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            Collections.singletonList(new ImportSpec(
                UUID.randomUUID(),
                Space.build("\n\t"),
                Markers.EMPTY,
                null,
                new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"io/ioutil\"")
            )),
            false,
            Space.EMPTY
        );
        
        java.util.List<Stmt> stmts = new java.util.ArrayList<>();
        if (ioutilMethod != null) {
            stmts.add(new ExprStmt(
                UUID.randomUUID(),
                Space.build("\n\t"),
                Markers.EMPTY,
                new CallExpr(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    new SelectorExpr(
                        UUID.randomUUID(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null),
                        new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ioutilMethod, null),
                        null
                    ),
                    Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "r", null)),
                    false,
                    null
                )
            ));
        }
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null),
            Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, stmts, Space.EMPTY)
        );
        
        return new GoFile(
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
            Collections.singletonList(importDecl),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
    }
    
    @Test
    void migrateIoutilReadAllToIOReadAll() {
        SelectorExpr selector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ReadAll", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            selector,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "r", null)),
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
        
        MigrateIoutilToIO recipe = new MigrateIoutilToIO();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("io", pkgIdent.getName());
        assertEquals("ReadAll", resultSelector.getSel().getName());
    }
    
    @Test
    void migrateIoutilReadFileToOSReadFile() {
        SelectorExpr selector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ReadFile", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            selector,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"test.txt\"")),
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
        
        MigrateIoutilToIO recipe = new MigrateIoutilToIO();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("os", pkgIdent.getName());
        assertEquals("ReadFile", resultSelector.getSel().getName());
    }
    
    @Test
    void migrateIoutilWriteFileToOSWriteFile() {
        SelectorExpr selector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "ioutil", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WriteFile", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            selector,
            java.util.Arrays.asList(
                new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"test.txt\""),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "data", null),
                new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0644")
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
        
        MigrateIoutilToIO recipe = new MigrateIoutilToIO();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("os", pkgIdent.getName());
        assertEquals("WriteFile", resultSelector.getSel().getName());
    }
    
    @Test
    void doesNotMigrateNonIoutilCalls() {
        SelectorExpr selector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Println", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            selector,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"")),
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
        
        MigrateIoutilToIO recipe = new MigrateIoutilToIO();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("fmt", pkgIdent.getName());
        assertEquals("Println", resultSelector.getSel().getName());
    }
}
