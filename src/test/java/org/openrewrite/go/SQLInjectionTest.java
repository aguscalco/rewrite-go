package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SQLInjectionTest {

    @Test
    void replacesStringConcatenation() {
        Ident dbIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "db", null);
        Ident queryIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Query", null);
        SelectorExpr dbQuerySel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, dbIdent, queryIdent, null);

        BasicLit strLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"SELECT * FROM users WHERE name = \"");
        Ident nameVar = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "name", null);
        
        BinaryExpr concat = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, strLit, "+", nameVar, null);

        CallExpr dbQueryCall = new CallExpr(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                dbQuerySel,
                Collections.singletonList(concat),
                false,
                null
        );

        SQLInjection recipe = new SQLInjection();
        Tree result = recipe.getVisitor().visit(dbQueryCall, null);

        assertNotSame(dbQueryCall, result);
        assertInstanceOf(CallExpr.class, result);
        CallExpr resultCall = (CallExpr) result;

        assertEquals(2, resultCall.getArgs().size());
        
        assertInstanceOf(BasicLit.class, resultCall.getArgs().get(0));
        assertEquals("\"SELECT * FROM users WHERE name = ?\"", ((BasicLit) resultCall.getArgs().get(0)).getValue());
        
        assertInstanceOf(Ident.class, resultCall.getArgs().get(1));
        assertEquals("name", ((Ident) resultCall.getArgs().get(1)).getName());
    }
}
