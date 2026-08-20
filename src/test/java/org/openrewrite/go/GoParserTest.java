package org.openrewrite.go;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.SourceFile;
import org.openrewrite.go.tree.GoFile;
import org.openrewrite.go.tree.GoPrinter;
import org.openrewrite.tree.ParseError;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * End-to-end coverage of the Java to Go bridge: real source in, a real LST out, via the
 * parser subprocess.
 * <p>
 * Skips when the binary has not been built, so `./gradlew test` still works without a Go
 * toolchain. Build it with `cd parser && go build -o rewrite-go-parser ./cmd/parser`.
 */
class GoParserTest {

    private static Path binary;

    @BeforeAll
    static void locateBinary() {
        binary = Paths.get("parser", "rewrite-go-parser").toAbsolutePath();
    }

    private GoParser parser() {
        assumeTrue(Files.isExecutable(binary),
            "rewrite-go-parser not built; run: cd parser && go build -o rewrite-go-parser ./cmd/parser");
        return GoParser.builder().parserBinary(binary).build();
    }

    private List<SourceFile> parse(String... sources) {
        List<Parser.Input> inputs = Arrays.stream(sources)
            .map(src -> Parser.Input.fromString(Paths.get("main.go"), src))
            .collect(Collectors.toList());
        return parser().parseInputs(inputs, null, new InMemoryExecutionContext()).collect(Collectors.toList());
    }

    private static String print(GoFile file) {
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        return capture.getOut();
    }

    @Test
    void parsesRealGoSourceIntoAnLst() {
        List<SourceFile> parsed = parse("package main\n");

        assertEquals(1, parsed.size());
        GoFile file = assertInstanceOf(GoFile.class, parsed.get(0));
        assertEquals("main", file.getPackageClause().getName().getName());
    }

    @Test
    void parsesImportsAndDeclarations() {
        List<SourceFile> parsed = parse("package main\n\nimport (\n\t\"fmt\"\n\t\"os\"\n)\n\nfunc main() {\n\tfmt.Println(\"hi\")\n}\n");

        GoFile file = assertInstanceOf(GoFile.class, parsed.get(0));
        assertEquals(1, file.getImports().size(), "a grouped import block is one declaration");
        assertEquals(2, file.getImports().get(0).getSpecs().size());
        assertEquals(1, file.getDeclarations().size());
    }

    /** The whole point of an LST: what comes out matches what went in. */
    @Test
    void printsBackWhatWasParsed() {
        String source = "package main\n\nimport \"fmt\"\n\nfunc greet(name string) string {\n\treturn name\n}\n";
        GoFile file = assertInstanceOf(GoFile.class, parse(source).get(0));
        assertEquals(source, print(file));
    }

    @Test
    void appliesTheCallersSourcePath() {
        GoFile file = assertInstanceOf(GoFile.class, parse("package main\n").get(0));
        assertEquals(Paths.get("main.go"), file.getSourcePath());
    }

    @Test
    void parsesABatchInOrder() {
        List<Parser.Input> inputs = Arrays.asList(
            Parser.Input.fromString(Paths.get("a.go"), "package alpha\n"),
            Parser.Input.fromString(Paths.get("b.go"), "package bravo\n"),
            Parser.Input.fromString(Paths.get("c.go"), "package charlie\n"));

        List<SourceFile> parsed = parser()
            .parseInputs(inputs, null, new InMemoryExecutionContext())
            .collect(Collectors.toList());

        assertEquals(3, parsed.size());
        assertEquals("alpha", ((GoFile) parsed.get(0)).getPackageClause().getName().getName());
        assertEquals("bravo", ((GoFile) parsed.get(1)).getPackageClause().getName().getName());
        assertEquals("charlie", ((GoFile) parsed.get(2)).getPackageClause().getName().getName());
    }

    /** One bad file must not take down the batch. */
    @Test
    void reportsASyntaxErrorWithoutFailingTheBatch() {
        List<Parser.Input> inputs = Arrays.asList(
            Parser.Input.fromString(Paths.get("good.go"), "package good\n"),
            Parser.Input.fromString(Paths.get("bad.go"), "package !!!\n"),
            Parser.Input.fromString(Paths.get("fine.go"), "package fine\n"));

        List<SourceFile> parsed = parser()
            .parseInputs(inputs, null, new InMemoryExecutionContext())
            .collect(Collectors.toList());

        assertEquals(3, parsed.size());
        assertInstanceOf(GoFile.class, parsed.get(0));
        assertInstanceOf(ParseError.class, parsed.get(1));
        assertInstanceOf(GoFile.class, parsed.get(2));
    }

    @Test
    void acceptsOnlyGoFiles() {
        GoParser p = parser();
        assertTrue(p.accept(Paths.get("main.go")));
        assertFalse(p.accept(Paths.get("main.java")));
        assertFalse(p.accept(Paths.get("go.mod")));
    }

    @Test
    void emptyInputDoesNotStartASubprocess() {
        // No assumeTrue: this must not need the binary at all.
        GoParser p = GoParser.builder().parserBinary(Paths.get("/nonexistent/parser")).build();
        assertEquals(0, p.parseInputs(java.util.Collections.emptyList(), null,
            new InMemoryExecutionContext()).count());
    }

    @Test
    void aMissingBinaryFailsWithAnActionableMessage() {
        GoParser p = GoParser.builder().parserBinary(Paths.get("/nonexistent/rewrite-go-parser")).build();
        List<Parser.Input> inputs = java.util.Collections.singletonList(
            Parser.Input.fromString(Paths.get("main.go"), "package main\n"));

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> p.parseInputs(inputs, null, new InMemoryExecutionContext()).count());
        assertTrue(e.getMessage().contains("go build -o rewrite-go-parser"), e.getMessage());
    }
}
