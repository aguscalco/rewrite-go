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

class UseMapsPackageTest {
    
    @Test
    void convertCopyMapToMapsClone() {
        // Create: result := copyMap(m)
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "copyMap", null),
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null)),
            false,
            null
        );
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "result", null)),
            ":=",
            Collections.singletonList(callExpr)
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
        
        UseMapsPackage recipe = new UseMapsPackage();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        AssignStmt resultAssign = (AssignStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultAssign.getRhs().get(0);
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("maps", pkgIdent.getName());
        assertEquals("Clone", resultSelector.getSel().getName());
        
        assertEquals(1, resultCall.getArgs().size());
        Ident argIdent = (Ident) resultCall.getArgs().get(0);
        assertEquals("m", argIdent.getName());
    }
    
    @Test
    void convertMapEqualsToMapsEqual() {
        // Create: equal := mapEquals(m1, m2)
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "mapEquals", null),
            Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m1", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "m2", null)
            ),
            false,
            null
        );
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "equal", null)),
            ":=",
            Collections.singletonList(callExpr)
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
        
        UseMapsPackage recipe = new UseMapsPackage();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        AssignStmt resultAssign = (AssignStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultAssign.getRhs().get(0);
        
        SelectorExpr resultSelector = (SelectorExpr) resultCall.getFun();
        Ident pkgIdent = (Ident) resultSelector.getX();
        assertEquals("maps", pkgIdent.getName());
        assertEquals("Equal", resultSelector.getSel().getName());
        
        assertEquals(2, resultCall.getArgs().size());
        Ident arg1 = (Ident) resultCall.getArgs().get(0);
        assertEquals("m1", arg1.getName());
        Ident arg2 = (Ident) resultCall.getArgs().get(1);
        assertEquals("m2", arg2.getName());
    }
    
    @Test
    void doNotConvertOtherFunctions() {
        // Create: result := otherFunc(m)
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "otherFunc", null),
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "m", null)),
            false,
            null
        );
        
        AssignStmt assignStmt = new AssignStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "result", null)),
            ":=",
            Collections.singletonList(callExpr)
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
        
        UseMapsPackage recipe = new UseMapsPackage();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        AssignStmt resultAssign = (AssignStmt) resultBody.getStmts().get(0);
        CallExpr resultCall = (CallExpr) resultAssign.getRhs().get(0);
        
        // Should remain unchanged
        Ident resultFuncIdent = (Ident) resultCall.getFun();
        assertEquals("otherFunc", resultFuncIdent.getName());
    }
}
