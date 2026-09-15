package org.openrewrite.go;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;

@Value
@EqualsAndHashCode(callSuper = true)
public class BenchmarkConversion extends Recipe {

    @Option(displayName = "Test function name",
            description = "The name of the test function to convert to a benchmark.",
            example = "TestProcess")
    String testName;

    @Override
    public String getDisplayName() {
        return "Convert test to benchmark";
    }

    @Override
    public String getDescription() {
        return "Converts a specified unit test into a benchmark by renaming it, changing the parameter to *testing.B, and wrapping the body in a b.N loop.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getName() != null && testName.equals(f.getName().getName())) {
                    // Rename TestX to BenchmarkX
                    String benchName = testName.replaceFirst("^Test", "Benchmark");
                    if (benchName.equals(testName)) {
                        benchName = "Benchmark" + testName;
                    }
                    f = f.withName(f.getName().withName(benchName));

                    // Change parameter from *testing.T to *testing.B
                    String bVarName = "b";
                    if (f.getType() != null && f.getType().getParams() != null && !f.getType().getParams().isEmpty()) {
                        Field param = f.getType().getParams().get(0);
                        if (param.getNames() != null && !param.getNames().isEmpty()) {
                            bVarName = param.getNames().get(0);
                        }
                        if (param.getType() instanceof StarExpr) {
                            StarExpr star = (StarExpr) param.getType();
                            if (star.getX() instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) star.getX();
                                if ("T".equals(sel.getSel().getName())) {
                                    SelectorExpr newSel = sel.withSel(sel.getSel().withName("B"));
                                    StarExpr newStar = star.withX(newSel);
                                    Field newParam = param.withType(newStar);
                                    f = f.withType(f.getType().withParams(Collections.singletonList(newParam)));
                                }
                            }
                        }
                    }

                    // Wrap body in for i := 0; i < b.N; i++ { ... }
                    if (f.getBody() != null) {
                        Ident iIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "i", null);
                        BasicLit zero = new BasicLit(Tree.randomId(), Space.build(" "), Markers.EMPTY, "INT", "0");
                        AssignStmt init = new AssignStmt(Tree.randomId(), Space.build("\n\t"), Markers.EMPTY,
                                Collections.singletonList(iIdent),
                                ":=",
                                Collections.singletonList(zero));

                        Ident bIdent = new Ident(Tree.randomId(), Space.build(" "), Markers.EMPTY, bVarName, null);
                        Ident nIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "N", null);
                        SelectorExpr bN = new SelectorExpr(Tree.randomId(), Space.build(" "), Markers.EMPTY, bIdent, nIdent, null);
                        BinaryExpr cond = new BinaryExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY,
                                iIdent.withPrefix(Space.build(" ")),
                                "<",
                                bN,
                                null);

                        IncDecStmt post = new IncDecStmt(Tree.randomId(), Space.EMPTY, Markers.EMPTY,
                                iIdent.withPrefix(Space.build(" ")),
                                "++");

                        ForStmt forStmt = new ForStmt(Tree.randomId(), Space.build("\n\t"), Markers.EMPTY,
                                init, cond, post, f.getBody().withPrefix(Space.build(" ")));

                        BlockStmt newBody = f.getBody().withStmts(Collections.singletonList(forStmt));
                        f = f.withBody(newBody);
                    }
                }

                return f;
            }
        };
    }
}
