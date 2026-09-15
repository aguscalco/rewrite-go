package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TLSConfigTest {

    @Test
    void addsMinVersionWhenMissing() {
        Ident tlsPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "tls", null);
        Ident configIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Config", null);
        SelectorExpr typeSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tlsPkg, configIdent, null);

        CompositeLit lit = new CompositeLit(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            typeSel,
            Collections.emptyList(),
            null
        );

        TLSConfig recipe = new TLSConfig();
        Tree result = recipe.getVisitor().visit(lit, null);

        assertNotSame(lit, result);
        assertInstanceOf(CompositeLit.class, result);
        CompositeLit resultLit = (CompositeLit) result;

        assertEquals(1, resultLit.getElts().size());
        assertInstanceOf(KeyValueExpr.class, resultLit.getElts().get(0));
        KeyValueExpr kv = (KeyValueExpr) resultLit.getElts().get(0);
        
        assertInstanceOf(Ident.class, kv.getKey());
        assertEquals("MinVersion", ((Ident) kv.getKey()).getName());
        
        assertInstanceOf(SelectorExpr.class, kv.getValue());
        SelectorExpr valSel = (SelectorExpr) kv.getValue();
        assertEquals("tls", ((Ident) valSel.getX()).getName());
        assertEquals("VersionTLS12", valSel.getSel().getName());
    }

    @Test
    void ignoresIfMinVersionPresent() {
        Ident tlsPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "tls", null);
        Ident configIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Config", null);
        SelectorExpr typeSel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tlsPkg, configIdent, null);

        Ident key = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "MinVersion", null);
        Ident valPkg = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "tls", null);
        Ident valVer = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "VersionTLS13", null);
        SelectorExpr val = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, valPkg, valVer, null);
        
        KeyValueExpr kv = new KeyValueExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, key, val);

        CompositeLit lit = new CompositeLit(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            typeSel,
            Collections.singletonList(kv),
            null
        );

        TLSConfig recipe = new TLSConfig();
        Tree result = recipe.getVisitor().visit(lit, null);

        assertSame(lit, result);
    }
}
