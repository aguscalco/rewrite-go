package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class SecureFilePermissions extends Recipe {

    @Override
    public String getDisplayName() {
        return "Secure file permissions (gosec G306)";
    }

    @Override
    public String getDescription() {
        return "Replaces overly permissive file creation modes (like `0777` or `0666`) with secure defaults (`0600`) in `os.OpenFile`, `os.WriteFile`, and `ioutil.WriteFile`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident) {
                        String pkg = ((Ident) sel.getX()).getName();
                        String method = sel.getSel().getName();
                        
                        int modeArgIdx = -1;
                        if ("os".equals(pkg)) {
                            if ("OpenFile".equals(method) && c.getArgs().size() == 3) {
                                modeArgIdx = 2;
                            } else if ("WriteFile".equals(method) && c.getArgs().size() == 3) {
                                modeArgIdx = 2;
                            }
                        } else if ("ioutil".equals(pkg) && "WriteFile".equals(method) && c.getArgs().size() == 3) {
                            modeArgIdx = 2;
                        }
                        
                        if (modeArgIdx >= 0) {
                            Expr modeArg = c.getArgs().get(modeArgIdx);
                            if (modeArg instanceof BasicLit) {
                                BasicLit lit = (BasicLit) modeArg;
                                if ("INT".equals(lit.getKind()) && ("0777".equals(lit.getValue()) || "0666".equals(lit.getValue()) || "0755".equals(lit.getValue()) || "0644".equals(lit.getValue()))) {
                                    
                                    java.util.List<Expr> newArgs = new java.util.ArrayList<>(c.getArgs());
                                    newArgs.set(modeArgIdx, new BasicLit(UUID.randomUUID(), lit.getPrefix(), Markers.EMPTY, "INT", "0600"));
                                    return c.withArgs(newArgs);
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
