package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class TempDirMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate manual temp directories to t.TempDir()";
    }

    @Override
    public String getDescription() {
        return "Flags usage of os.MkdirTemp or ioutil.TempDir in tests, recommending the use of t.TempDir() (Go 1.15+) which automatically handles cleanup.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident) {
                        String pkg = ((Ident) sel.getX()).getName();
                        String method = sel.getSel().getName();
                        
                        if (("os".equals(pkg) && "MkdirTemp".equals(method)) || 
                            ("ioutil".equals(pkg) && "TempDir".equals(method))) {
                            
                            // Check if we are inside a testing function (has a *testing.T or *testing.B param)
                            FuncDecl funcDecl = getCursor().firstEnclosing(FuncDecl.class);
                            if (funcDecl != null && isTestFunction(funcDecl)) {
                                return SearchResult.found(c, "Go 1.15+: Use t.TempDir() to automatically create and clean up temporary directories in tests.");
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isTestFunction(FuncDecl funcDecl) {
                if (funcDecl.getType() != null && funcDecl.getType().getParams() != null) {
                    for (Field param : funcDecl.getType().getParams()) {
                        if (param.getType() instanceof StarExpr) {
                            Expr starX = ((StarExpr) param.getType()).getX();
                            if (starX instanceof SelectorExpr) {
                                SelectorExpr sel = (SelectorExpr) starX;
                                if (sel.getX() instanceof Ident && "testing".equals(((Ident) sel.getX()).getName())) {
                                    String typeName = sel.getSel().getName();
                                    if ("T".equals(typeName) || "B".equals(typeName) || "F".equals(typeName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        };
    }
}
