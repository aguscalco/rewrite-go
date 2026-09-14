package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParameterizedQueriesTest {

    @Test
    void replacesFmtSprintfInQuery() {
        // Construct AST for: db.Query(fmt.Sprintf("SELECT * FROM users WHERE name = '%s'", name))
        Ident dbIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "db", null);
        Ident queryIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Query", null);
        SelectorExpr dbQuerySel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, dbIdent, queryIdent, null);

        Ident fmtIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null);
        Ident sprintfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Sprintf", null);
        SelectorExpr fmtSprintfSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, fmtIdent, sprintfIdent, null);

        BasicLit formatLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"SELECT * FROM users WHERE name = '%s'\"");
        Ident nameIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "name", null);

        CallExpr sprintfCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            fmtSprintfSel,
            Arrays.asList(formatLit, nameIdent),
            false,
            null
        );

        CallExpr dbQueryCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            dbQuerySel,
            Collections.singletonList(sprintfCall),
            false,
            null
        );

        ParameterizedQueries recipe = new ParameterizedQueries();
        Tree result = recipe.getVisitor().visit(dbQueryCall, null);

        assertNotSame(dbQueryCall, result);
        assertInstanceOf(CallExpr.class, result);
        CallExpr resultCall = (CallExpr) result;

        assertEquals(2, resultCall.getArgs().size());
        
        assertInstanceOf(BasicLit.class, resultCall.getArgs().get(0));
        BasicLit resultFormat = (BasicLit) resultCall.getArgs().get(0);
        assertEquals("\"SELECT * FROM users WHERE name = ?\"", resultFormat.getValue());
        
        assertInstanceOf(Ident.class, resultCall.getArgs().get(1));
        Ident resultArg = (Ident) resultCall.getArgs().get(1);
        assertEquals("name", resultArg.getName());
    }

    @Test
    void ignoresSafeQueries() {
        // Construct AST for: db.Query("SELECT * FROM users WHERE name = ?", name)
        Ident dbIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "db", null);
        Ident queryIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Query", null);
        SelectorExpr dbQuerySel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, dbIdent, queryIdent, null);

        BasicLit queryLit = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"SELECT * FROM users WHERE name = ?\"");
        Ident nameIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "name", null);

        CallExpr dbQueryCall = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            dbQuerySel,
            Arrays.asList(queryLit, nameIdent),
            false,
            null
        );

        ParameterizedQueries recipe = new ParameterizedQueries();
        Tree result = recipe.getVisitor().visit(dbQueryCall, null);

        assertSame(dbQueryCall, result);
    }
}
