package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class StringBytesString extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove unnecessary string([]byte(s)) conversions";
    }

    @Override
    public String getDescription() {
        return "Migrates `string([]byte(s))` back to `s` when `s` is already a string.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof Ident && "string".equals(((Ident) c.getFun()).getName())) {
                    if (c.getArgs() != null && c.getArgs().size() == 1) {
                        Expr arg = c.getArgs().get(0);
                        
                        if (arg instanceof CallExpr) {
                            CallExpr byteCast = (CallExpr) arg;
                            if (byteCast.getFun() instanceof ArrayTypeExpr) {
                                ArrayTypeExpr arr = (ArrayTypeExpr) byteCast.getFun();
                                if (arr.getElt() instanceof Ident && "byte".equals(((Ident) arr.getElt()).getName()) && arr.getLen() == null) {
                                    if (byteCast.getArgs() != null && byteCast.getArgs().size() == 1) {
                                        return byteCast.getArgs().get(0).withPrefix(c.getPrefix());
                                    }
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
