package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecureDirectoryPermissionsTest {

    @Test
    void migratesInsecureMkdir() {
        Ident osIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "os", null);
        Ident mkdirIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Mkdir", null);
        SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osIdent, mkdirIdent, null);
        
        Ident pathIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "path", null);
        BasicLit badMode = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "0777");
        CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(pathIdent, badMode), false, null);

        SecureDirectoryPermissions recipe = new SecureDirectoryPermissions();
        Tree result = recipe.getVisitor().visit(call, null);

        assertNotSame(call, result);
        assertTrue(result instanceof CallExpr);
        CallExpr resCall = (CallExpr) result;
        BasicLit newMode = (BasicLit) resCall.getArgs().get(1);
        assertEquals("0750", newMode.getValue());
    }
}
