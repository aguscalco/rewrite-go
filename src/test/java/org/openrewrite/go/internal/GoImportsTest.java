package org.openrewrite.go.internal;

import org.junit.jupiter.api.Test;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GoImportsTest {

    private static GoFile fileWith(String... paths) {
        List<ImportDecl> decls = new ArrayList<>();
        if (paths.length > 0) {
            List<ImportSpec> specs = new ArrayList<>();
            for (String path : paths) {
                specs.add(new ImportSpec(
                    UUID.randomUUID(), Space.build("\n\t"), Markers.EMPTY, null,
                    new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"" + path + "\"")));
            }
            decls.add(new ImportDecl(
                UUID.randomUUID(), Space.build("\n"), Markers.EMPTY, specs, specs.size() > 1, Space.EMPTY));
        }
        return new GoFile(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Paths.get("test.go"), StandardCharsets.UTF_8, false,
            new PackageClause(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)),
            decls, Collections.emptyList(), Space.EMPTY, null, null);
    }

    private static List<String> paths(GoFile file) {
        List<String> out = new ArrayList<>();
        for (ImportDecl decl : file.getImports()) {
            for (ImportSpec spec : decl.getSpecs()) {
                out.add(GoImports.unquote(spec.getPath().getValue()));
            }
        }
        return out;
    }

    private static String print(GoFile file) {
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        return capture.getOut();
    }

    @Test
    void addsAnImportToAFileThatHasNone() {
        assertEquals(Collections.singletonList("errors"), paths(GoImports.addImport(fileWith(), "errors")));
    }

    @Test
    void addsAnImportAlongsideExistingOnes() {
        assertEquals(Arrays.asList("fmt", "errors"), paths(GoImports.addImport(fileWith("fmt"), "errors")));
    }

    @Test
    void addingAnExistingImportIsANoOp() {
        GoFile file = fileWith("fmt");
        assertSame(file, GoImports.addImport(file, "fmt"));
    }

    @Test
    void removesAnImport() {
        assertEquals(Collections.singletonList("os"), paths(GoImports.removeImport(fileWith("sort", "os"), "sort")));
    }

    @Test
    void removingTheLastImportDropsTheDeclaration() {
        assertTrue(GoImports.removeImport(fileWith("sort"), "sort").getImports().isEmpty());
    }

    @Test
    void removingAnAbsentImportIsANoOp() {
        GoFile file = fileWith("fmt");
        assertSame(file, GoImports.removeImport(file, "sort"));
    }

    @Test
    void hasImportIgnoresQuoting() {
        assertTrue(GoImports.hasImport(fileWith("io/ioutil"), "io/ioutil"));
        assertFalse(GoImports.hasImport(fileWith("io/ioutil"), "io"));
    }

    /**
     * GoPrinter only prints the first spec of an ungrouped declaration, so adding a second
     * import must flip {@code grouped} or the new import is dropped on print.
     */
    @Test
    void aSecondImportSurvivesPrinting() {
        String out = print(GoImports.addImport(fileWith("fmt"), "errors"));
        assertTrue(out.contains("\"fmt\""), out);
        assertTrue(out.contains("\"errors\""), out);
        assertTrue(out.contains("import ("), out);
        assertTrue(out.endsWith(")"), out);
    }

    @Test
    void removingDownToOneImportUngroupsIt() {
        GoFile file = GoImports.removeImport(fileWith("fmt", "errors"), "errors");
        assertFalse(file.getImports().get(0).isGrouped());
        assertTrue(print(file).contains("\"fmt\""));
    }
}
