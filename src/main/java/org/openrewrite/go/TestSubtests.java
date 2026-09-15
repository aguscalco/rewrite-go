package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;

/**
 * Converts range loops in test functions into t.Run subtests.
 */
public class TestSubtests extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert test loops to subtests";
    }

    @Override
    public String getDescription() {
        return "Wraps the body of test loops in t.Run to create proper subtests, improving test isolation and output formatting.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            private String testingVarName = null;

            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                String oldTestingVar = testingVarName;
                testingVarName = null;

                if (funcDecl.getName() != null && funcDecl.getName().getName().startsWith("Test")) {
                    if (funcDecl.getType() != null && funcDecl.getType().getParams() != null) {
                        for (Field param : funcDecl.getType().getParams()) {
                            if (param.getType() instanceof StarExpr) {
                                StarExpr star = (StarExpr) param.getType();
                                if (star.getX() instanceof SelectorExpr) {
                                    SelectorExpr sel = (SelectorExpr) star.getX();
                                    if (sel.getX() instanceof Ident && "testing".equals(((Ident) sel.getX()).getName()) &&
                                        "T".equals(sel.getSel().getName())) {
                                        if (param.getNames() != null && !param.getNames().isEmpty()) {
                                            testingVarName = param.getNames().get(0);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Tree result = super.visitFuncDecl(funcDecl, ctx);
                testingVarName = oldTestingVar;
                return result;
            }

            @Override
            public Tree visitRangeStmt(RangeStmt rangeStmt, ExecutionContext ctx) {
                RangeStmt r = (RangeStmt) super.visitRangeStmt(rangeStmt, ctx);

                if (testingVarName == null || r.getBody() == null || r.getBody().getStmts() == null || r.getBody().getStmts().isEmpty()) {
                    return r;
                }

                // Check if it already has t.Run
                if (r.getBody().getStmts().size() == 1) {
                    Stmt stmt = r.getBody().getStmts().get(0);
                    if (stmt instanceof ExprStmt && ((ExprStmt) stmt).getExpr() instanceof CallExpr) {
                        CallExpr call = (CallExpr) ((ExprStmt) stmt).getExpr();
                        if (call.getFun() instanceof SelectorExpr) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            if (sel.getX() instanceof Ident && testingVarName.equals(((Ident) sel.getX()).getName()) &&
                                "Run".equals(sel.getSel().getName())) {
                                return r;
                            }
                        }
                    }
                }

                // Determine name for subtest
                Expr subtestName = new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"subtest\"");
                
                // If ranging over a map/slice, and there's a struct with 'name' or 'Name' field, use it.
                // For simplicity, we just use the loop variable + .name if available, but doing that generically
                // without type info is hard. We'll just generate `"test"` or attempt to use `tt.name` if the var is `tt`.
                if (r.getValue() != null && r.getValue() instanceof Ident) {
                    Ident loopVar = (Ident) r.getValue();
                    subtestName = new SelectorExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, 
                        loopVar.withPrefix(Space.EMPTY), 
                        new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "name", null), null);
                }

                // Create t.Run(subtestName, func(t *testing.T) { ... })
                Ident tIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, testingVarName, null);
                Ident runIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "Run", null);
                SelectorExpr runSel = new SelectorExpr(Tree.randomId(), Space.build("\n\t\t"), Markers.EMPTY, tIdent, runIdent, null);

                // Build func(t *testing.T)
                Ident testingPkg = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "testing", null);
                Ident tType = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "T", null);
                SelectorExpr testingTSel = new SelectorExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, testingPkg, tType, null);
                StarExpr ptrType = new StarExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, testingTSel, null);
                Field param = new Field(Tree.randomId(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(testingVarName), ptrType, null);
                FuncType funcType = new FuncType(Tree.randomId(), Space.EMPTY, Markers.EMPTY, Collections.singletonList(param), null, null);

                FuncLit funcLit = new FuncLit(
                        Tree.randomId(),
                        Space.build(" "),
                        Markers.EMPTY,
                        funcType,
                        r.getBody()
                );

                CallExpr tRunCall = new CallExpr(
                        Tree.randomId(),
                        Space.EMPTY,
                        Markers.EMPTY,
                        runSel,
                        Arrays.asList(subtestName, funcLit),
                        false,
                        null
                );

                ExprStmt runStmt = new ExprStmt(Tree.randomId(), Space.EMPTY, Markers.EMPTY, tRunCall);
                BlockStmt newBody = r.getBody().withStmts(Collections.singletonList(runStmt));
                
                return r.withBody(newBody);
            }
        };
    }
}
