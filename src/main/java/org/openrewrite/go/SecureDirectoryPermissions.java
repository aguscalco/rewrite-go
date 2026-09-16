package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class SecureDirectoryPermissions extends Recipe {

    @Override
    public String getDisplayName() {
        return "Secure directory permissions (gosec G301)";
    }

    @Override
    public String getDescription() {
        return "Replaces overly permissive directory creation modes (like `0777`) with secure defaults (`0750`) in `os.Mkdir` and `os.MkdirAll`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "os".equals(((Ident) sel.getX()).getName())) {
                        String method = sel.getSel().getName();
                        if ("Mkdir".equals(method) || "MkdirAll".equals(method)) {
                            
                            Expr modeArg = c.getArgs().get(1);
                            if (modeArg instanceof BasicLit) {
                                BasicLit lit = (BasicLit) modeArg;
                                if ("INT".equals(lit.getKind()) && ("0777".equals(lit.getValue()) || "0775".equals(lit.getValue()))) {
                                    return c.withArgs(java.util.Arrays.asList(
                                        c.getArgs().get(0),
                                        new BasicLit(UUID.randomUUID(), lit.getPrefix(), Markers.EMPTY, "INT", "0750")
                                    ));
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
