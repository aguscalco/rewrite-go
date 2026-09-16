package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecureFilePermissionsTest {

    @Test
    void migratesInsecureWriteFile() {
        Ident osIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident writeFileIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "WriteFile", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osIdent, writeFileIdent, null);
        
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        Ident dataIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "data", null);
        BasicLit badMode = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0666");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(pathIdent, dataIdent, badMode), false, null);

        SecureFilePermissions recipe = new SecureFilePermissions();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        BasicLit newMode = (BasicLit) resCall.getArgs().get(2);
        assertEquals("0600", newMode.getValue());
    }
}
