package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddContextParameterTest {
    
    @Test
    void addsContextToFunctionWithoutContext() {
        FuncType funcType = new FuncType(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.singletonList("data"),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null),
                    null
                )
            ),
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null),
                    null
                )
            ),
            null
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "processData", null),
            null,
            funcType,
            new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        AddContextParameter recipe = new AddContextParameter();
        FuncDecl result = (FuncDecl) recipe.getVisitor().visit(funcDecl, null);
        
        assertEquals(2, result.getType().getParams().size());
        Field ctxParam = result.getType().getParams().get(0);
        assertEquals("ctx", ctxParam.getNames().get(0));
        assertTrue(ctxParam.getType() instanceof SelectorExpr);
        SelectorExpr sel = (SelectorExpr) ctxParam.getType();
        assertEquals("context", ((Ident) sel.getX()).getName());
        assertEquals("Context", sel.getSel().getName());
    }
    
    @Test
    void doesNotAddContextWhenAlreadyPresent() {
        Ident contextIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "context", null);
        Ident contextTypeIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Context", null);
        SelectorExpr contextSelector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            contextIdent,
            contextTypeIdent,
            null
        );
        
        FuncType funcType = new FuncType(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.singletonList("ctx"),
                    contextSelector,
                    null
                ),
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.singletonList("data"),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null),
                    null
                )
            ),
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null),
                    null
                )
            ),
            null
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "processData", null),
            null,
            funcType,
            new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        AddContextParameter recipe = new AddContextParameter();
        FuncDecl result = (FuncDecl) recipe.getVisitor().visit(funcDecl, null);
        
        assertEquals(2, result.getType().getParams().size());
        assertSame(funcDecl, result);
    }
    
    @Test
    void doesNotAddContextToFunctionWithNoParams() {
        FuncType funcType = new FuncType(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.emptyList(),
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null),
                    null
                )
            ),
            null
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "getData", null),
            null,
            funcType,
            new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        AddContextParameter recipe = new AddContextParameter();
        FuncDecl result = (FuncDecl) recipe.getVisitor().visit(funcDecl, null);
        
        assertEquals(0, result.getType().getParams().size());
        assertSame(funcDecl, result);
    }
    
    @Test
    void respectsFunctionNamePattern() {
        FuncType funcType = new FuncType(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.singletonList("id"),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                    null
                )
            ),
            Arrays.asList(
                new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "string", null),
                    null
                )
            ),
            null
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "getData", null),
            null,
            funcType,
            new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        AddContextParameter recipe = new AddContextParameter();
        recipe.functionNamePattern = "process.*";
        FuncDecl result = (FuncDecl) recipe.getVisitor().visit(funcDecl, null);
        
        assertEquals(1, result.getType().getParams().size());
        assertSame(funcDecl, result);
    }
}
