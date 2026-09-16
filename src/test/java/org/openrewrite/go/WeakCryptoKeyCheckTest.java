package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WeakCryptoKeyCheckTest {

    @Test
    void upgradesRsaKeyLength() {
        Ident rsaIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "rsa", null);
        Ident genIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "GenerateKey", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, rsaIdent, genIdent, null);
        
        Ident randIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "randReader", null);
        BasicLit weakBits = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "1024");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(randIdent, weakBits), false, null);

        WeakCryptoKeyCheck recipe = new WeakCryptoKeyCheck();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        CallExpr resCall = (CallExpr) result;
        BasicLit newBits = (BasicLit) resCall.getArgs().get(1);
        assertEquals("2048", newBits.getValue());
    }
}
