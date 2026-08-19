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

class UseSlicesPackageTest {
    
    @Test
    void convertSortSliceToSlicesSort() {
        // Create: sort.Slice(data, func(i, j int) bool { return data[i] < data[j] })
        SelectorExpr sortSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Slice", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            sortSelector,
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "less", null)
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
        
        UseSlicesPackage recipe = new UseSlicesPackage();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("slices", pkgIdent.getName());
        assertEquals("Sort", resultSelector.getSel().getName());
        
        // Should only have one argument (the slice)
        assertEquals(1, resultCall.getArgs().size());
        Ident sliceArg = (Ident) resultCall.getArgs().get(0);
        assertEquals("data", sliceArg.getName());
    }
    
    @Test
    void convertSortSliceStableToSlicesSortStable() {
        // Create: sort.SliceStable(data, less)
        SelectorExpr sortSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "sort", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "SliceStable", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            sortSelector,
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "less", null)
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
        
        UseSlicesPackage recipe = new UseSlicesPackage();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ExprStmt resultStmt = (ExprStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultStmt.getExpr();
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("slices", pkgIdent.getName());
        assertEquals("SortStable", resultSelector.getSel().getName());
        
        // Should only have one argument (the slice)
        assertEquals(1, resultCall.getArgs().size());
    }
    
    @Test
    void doNotConvertNonSortCalls() {
        // Create: fmt.Println("hello")
        SelectorExpr fmtSelector = new SelectorExpr(
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
            fmtSelector,
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
        
        UseSlicesPackage recipe = new UseSlicesPackage();
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
        assertEquals("fmt", pkgIdent.getName());
        assertEquals("Println", resultSelector.getSel().getName());
    }
}
