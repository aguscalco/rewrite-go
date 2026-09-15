package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

import java.util.List;

public class FuzzTestConversion extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert table-driven tests to Fuzz tests";
    }

    @Override
    public String getDescription() {
        return "Flags table-driven tests (TestXxx containing a RangeStmt) for potential migration to Go 1.18+ Fuzz tests (testing.F).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public FuncDecl visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                String name = f.getName().getName();
                if (name.startsWith("Test") && f.getType() != null && f.getType().getParams() != null) {
                    // Check if it takes *testing.T
                    boolean hasTestingT = false;
                    for (Field param : f.getType().getParams()) {
                        if (param.getType() instanceof StarExpr) {
                            Expr starX = ((StarExpr) param.getType()).getX();
                            if (starX instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) starX;
                                if (sel.getX() instanceof Ident && "testing".equals(((Ident) sel.getX()).getName()) &&
                                    "T".equals(sel.getSel().getName())) {
                                    hasTestingT = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (hasTestingT && f.getBody() != null) {
                        // Look for a RangeStmt (table-driven test loop)
                        boolean hasRange = false;
                        for (Stmt stmt : f.getBody().getStmts()) {
                            if (stmt instanceof RangeStmt) {
                                hasRange = true;
                                break;
                            }
                        }
                        
                        if (hasRange) {
                            return SearchResult.found(f, "Go 1.18+: Consider converting this table-driven test to a Fuzz test (testing.F)");
                        }
                    }
                }
                
                return f;
            }
        };
    }
}
