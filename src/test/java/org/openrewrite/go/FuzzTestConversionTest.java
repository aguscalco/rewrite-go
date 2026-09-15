package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FuzzTestConversionTest {

    @Test
    void flagsTableDrivenTest() {
        Ident testingIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "testing", null);
        Ident tIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "T", null);
        SelectorExpr testingT = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingIdent, tIdent, null);
        StarExpr starTestingT = new StarExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, testingT, null);
        Field param = new Field(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList("t"), starTestingT, null);
        
        FuncType funcType = new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);
        Ident funcName = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "TestSomething", null);
        
        RangeStmt rangeStmt = new RangeStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, null, null, null, null);
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(rangeStmt), Space.EMPTY);
        
        FuncDecl func = new FuncDecl(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, funcName, null, funcType, body);

        FuzzTestConversion recipe = new FuzzTestConversion();
        Tree result = recipe.getVisitor().visit(func, null);

        assertNotSame(func, result);
        FuncDecl resultFunc = (FuncDecl) result;
        assertTrue(resultFunc.getMarkers().getMarkers().stream().anyMatch(m -> m instanceof SearchResult));
    }
}
