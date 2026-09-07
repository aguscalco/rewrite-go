package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RangeOverIntegersTest {
    
    @Test
    void convertsSimpleCountingLoop() {
        ForStmt forStmt = new ForStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new AssignStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null)),
                ":=",
                Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0"))
            ),
            new BinaryExpr(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "<",
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "10", null),
                null
            ),
            new IncDecStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "++"
            ),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        RangeOverIntegers recipe = new RangeOverIntegers();
        Tree result = recipe.getVisitor().visit(forStmt, null);
        
        assertTrue(result instanceof RangeStmt);
        RangeStmt rangeStmt = (RangeStmt) result;
        
        assertEquals("i", ((Ident) rangeStmt.getKey()).getName());
        assertNull(rangeStmt.getValue());
        assertEquals("10", ((Ident) rangeStmt.getX()).getName());
        assertEquals(":=", rangeStmt.getTok());
    }
    
    @Test
    void doesNotConvertLoopStartingAtNonZero() {
        ForStmt forStmt = new ForStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new AssignStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null)),
                ":=",
                Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "1"))
            ),
            new BinaryExpr(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "<",
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "10", null),
                null
            ),
            new IncDecStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "++"
            ),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        RangeOverIntegers recipe = new RangeOverIntegers();
        Tree result = recipe.getVisitor().visit(forStmt, null);
        
        assertTrue(result instanceof ForStmt);
    }
    
    @Test
    void doesNotConvertLoopWithDifferentOperator() {
        ForStmt forStmt = new ForStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new AssignStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null)),
                ":=",
                Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0"))
            ),
            new BinaryExpr(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "<=",
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "10", null),
                null
            ),
            new IncDecStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "++"
            ),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        RangeOverIntegers recipe = new RangeOverIntegers();
        Tree result = recipe.getVisitor().visit(forStmt, null);
        
        assertTrue(result instanceof ForStmt);
    }
    
    @Test
    void doesNotConvertLoopWithDecrement() {
        ForStmt forStmt = new ForStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new AssignStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null)),
                ":=",
                Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0"))
            ),
            new BinaryExpr(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "<",
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "10", null),
                null
            ),
            new IncDecStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "--"
            ),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        RangeOverIntegers recipe = new RangeOverIntegers();
        Tree result = recipe.getVisitor().visit(forStmt, null);
        
        assertTrue(result instanceof ForStmt);
    }
    
    @Test
    void doesNotConvertLoopWithDifferentVariableNames() {
        ForStmt forStmt = new ForStmt(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new AssignStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null)),
                ":=",
                Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0"))
            ),
            new BinaryExpr(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "j", null),
                "<",
                new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "10", null),
                null
            ),
            new IncDecStmt(
                UUID.randomUUID(),
                Space.build(" "),
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "i", null),
                "++"
            ),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        RangeOverIntegers recipe = new RangeOverIntegers();
        Tree result = recipe.getVisitor().visit(forStmt, null);
        
        assertTrue(result instanceof ForStmt);
    }
}
