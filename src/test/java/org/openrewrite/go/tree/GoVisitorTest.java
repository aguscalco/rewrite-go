package org.openrewrite.go.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.marker.Markers;
import org.openrewrite.text.PlainText;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Traversal coverage for {@link GoVisitor}. Several visit methods used to return the node
 * without descending, so a recipe targeting a nested node never fired.
 */
class GoVisitorTest {

    /** Records the name of every Ident reached by traversal. */
    private static class IdentCollector extends GoVisitor<ExecutionContext> {
        final List<String> seen = new ArrayList<>();

        @Override
        public Tree visitIdent(Ident ident, ExecutionContext ctx) {
            seen.add(ident.getName());
            return ident;
        }
    }

    private static Ident ident(String name) {
        return new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, name, null);
    }

    private static CallExpr call(String pkg, String fn) {
        return new CallExpr(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ident(pkg), ident(fn), null),
            Collections.emptyList(), false, null);
    }

    private static GoFile fileWith(Stmt... stmts) {
        BlockStmt body = new BlockStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Arrays.asList(stmts), Space.EMPTY);
        FuncDecl fn = new FuncDecl(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, null, ident("main"), Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            body);
        return new GoFile(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Paths.get("test.go"), StandardCharsets.UTF_8, false,
            new PackageClause(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ident("main")),
            Collections.emptyList(), Collections.singletonList(fn), Space.EMPTY, null, null);
    }

    private static List<String> visit(GoFile file) {
        IdentCollector collector = new IdentCollector();
        collector.visit(file, new InMemoryExecutionContext());
        return collector.seen;
    }

    @Test
    void descendsIntoAssignStmtBothSides() {
        // lhs, ok := pkg.Fn()
        AssignStmt assign = new AssignStmt(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            Arrays.asList(ident("lhs"), ident("ok")), ":=",
            Collections.singletonList(call("pkg", "Fn")));

        List<String> seen = visit(fileWith(assign));
        assertTrue(seen.contains("lhs"), seen.toString());
        assertTrue(seen.contains("ok"), seen.toString());
        assertTrue(seen.contains("pkg"), seen.toString());
        assertTrue(seen.contains("Fn"), seen.toString());
    }

    @Test
    void descendsIntoReturnStmt() {
        ReturnStmt ret = new ReturnStmt(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            Collections.singletonList(call("pkg", "Fn")));

        assertTrue(visit(fileWith(ret)).contains("Fn"));
    }

    @Test
    void descendsIntoBinaryExprOperands() {
        ExprStmt stmt = new ExprStmt(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
                ident("left"), "==", ident("right"), null));

        List<String> seen = visit(fileWith(stmt));
        assertTrue(seen.contains("left"), seen.toString());
        assertTrue(seen.contains("right"), seen.toString());
    }

    @Test
    void descendsIntoUnaryExprOperand() {
        ExprStmt stmt = new ExprStmt(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            new UnaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "!", ident("flag"), null));

        assertTrue(visit(fileWith(stmt)).contains("flag"));
    }

    @Test
    void descendsIntoCompositeTypeExpressions() {
        // map[key][]*elem
        SliceTypeExpr slice = new SliceTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            new PointerTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ident("elem")));
        MapTypeExpr map = new MapTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ident("key"), slice);

        ExprStmt stmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, map);

        List<String> seen = visit(fileWith(stmt));
        assertTrue(seen.contains("key"), seen.toString());
        assertTrue(seen.contains("elem"), seen.toString());
    }

    @Test
    void descendsIntoGenDeclSpecs() {
        ValueSpec spec = new ValueSpec(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
            Collections.singletonList(ident("x")), null,
            Collections.singletonList(call("pkg", "Fn")));
        GenDecl decl = new GenDecl(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "var",
            Collections.singletonList(spec), false, Space.EMPTY);

        GoFile file = new GoFile(
            UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Paths.get("test.go"), StandardCharsets.UTF_8, false,
            new PackageClause(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, ident("main")),
            Collections.emptyList(), Collections.singletonList(decl), Space.EMPTY, null, null);

        List<String> seen = visit(file);
        assertTrue(seen.contains("x"), seen.toString());
        assertTrue(seen.contains("Fn"), seen.toString());
    }

    @Test
    void descendsIntoPackageClause() {
        assertTrue(visit(fileWith()).contains("main"));
    }

    @Test
    void isAcceptableOverridesTheSourceFileOverload() {
        GoVisitor<ExecutionContext> visitor = new GoVisitor<>();
        SourceFile go = fileWith();
        SourceFile notGo = PlainText.builder().sourcePath(Paths.get("a.txt")).text("hi").build();

        assertTrue(visitor.isAcceptable(go, new InMemoryExecutionContext()));
        assertFalse(visitor.isAcceptable(notGo, new InMemoryExecutionContext()));
    }
}
