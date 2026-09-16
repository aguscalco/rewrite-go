package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class PathJoinEmptyString extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use path.Clean over path.Join with empty string";
    }

    @Override
    public String getDescription() {
        return "Migrates `path.Join(p, \"\")` to `path.Clean(p)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "path".equals(((Ident) sel.getX()).getName()) && "Join".equals(sel.getSel().getName())) {
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        if (isEmptyString(arg1)) {
                            return toClean(c, arg2);
                        } else if (isEmptyString(arg2)) {
                            return toClean(c, arg1);
                        }
                    }
                }

                return c;
            }

            private boolean isEmptyString(Expr expr) {
                if (expr instanceof BasicLit) {
                    String val = ((BasicLit) expr).getValue();
                    return "\"\"".equals(val) || "``".equals(val);
                }
                return false;
            }
            
            private Tree toClean(CallExpr call, Expr pathArg) {
                SelectorExpr sel = (SelectorExpr) call.getFun();
                Ident cleanIdent = sel.getSel().withName("Clean");
                return call.withFun(sel.withSel(cleanIdent)).withArgs(Collections.singletonList(pathArg.withPrefix(Space.EMPTY)));
            }
        };
    }
}
