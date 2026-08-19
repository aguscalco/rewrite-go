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

class WrapErrorWithContextTest {
    
    @Test
    void wrapErrorWithFmtErrorf() {
        // Create: return err
        ReturnStmt returnStmt = new ReturnStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "err", null))
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(returnStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "doSomething", null),
            Collections.emptyList(),
            new FuncType(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
                Collections.singletonList(new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null),
                    ""
                )),
                Collections.emptyList()
            ),
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
        
        WrapErrorWithContext recipe = new WrapErrorWithContext();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        assertNotNull(result);
        assertTrue(result instanceof GoFile);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        assertEquals(1, resultBody.getStmts().size());
        
        ReturnStmt resultReturn = (ReturnStmt) resultBody.getStmts().get(0);
        assertEquals(1, resultReturn.getResults().size());
        
        // Should be transformed to: return fmt.Errorf("context: %w", err)
        CallExpr callExpr = (CallExpr) resultReturn.getResults().get(0);
        assertNotNull(callExpr);
        
        SelectorExpr selector = (SelectorExpr) callExpr.getFun();
        Ident pkgIdent = (Ident) selector.getX();
        assertEquals("fmt", pkgIdent.getName());
        assertEquals("Errorf", selector.getSel().getName());
        
        assertEquals(2, callExpr.getArgs().size());
        BasicLit formatString = (BasicLit) callExpr.getArgs().get(0);
        assertTrue(formatString.getValue().contains("%w"));
        
        Ident errArg = (Ident) callExpr.getArgs().get(1);
        assertEquals("err", errArg.getName());
    }
    
    @Test
    void doesNotWrapNonErrorReturn() {
        // Create: return 42
        ReturnStmt returnStmt = new ReturnStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "42"))
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(returnStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "getNumber", null),
            Collections.emptyList(),
            new FuncType(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
                Collections.singletonList(new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                        new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                    ""
                )),
                Collections.emptyList()
            ),
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
        
        WrapErrorWithContext recipe = new WrapErrorWithContext();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ReturnStmt resultReturn = (ReturnStmt) resultBody.getStmts().get(0);
        
        // Should remain unchanged
        BasicLit basicLit = (BasicLit) resultReturn.getResults().get(0);
        assertEquals("42", basicLit.getValue());
    }
    
    @Test
    void doesNotWrapMultipleReturnValues() {
        // Create: return result, err
        ReturnStmt returnStmt = new ReturnStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            java.util.Arrays.asList(
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "result", null),
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "err", null)
            )
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(returnStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "doSomething", null),
            Collections.emptyList(),
            new FuncType(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
                java.util.Arrays.asList(
                    new Field(
                        UUID.randomUUID(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                        ""
                    ),
                    new Field(
                        UUID.randomUUID(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        Collections.emptyList(),
                        new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null),
                        ""
                    )
                ),
                Collections.emptyList()
            ),
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
        
        WrapErrorWithContext recipe = new WrapErrorWithContext();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        BlockStmt resultBody = resultFunc.getBody();
        
        ReturnStmt resultReturn = (ReturnStmt) resultBody.getStmts().get(0);
        
        // Should remain unchanged (multiple return values)
        assertEquals(2, resultReturn.getResults().size());
        Ident resultIdent = (Ident) resultReturn.getResults().get(0);
        assertEquals("result", resultIdent.getName());
        Ident errIdent = (Ident) resultReturn.getResults().get(1);
        assertEquals("err", errIdent.getName());
    }
}
