package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class BytesStringBytes extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove unnecessary []byte(string(b)) conversions";
    }

    @Override
    public String getDescription() {
        return "Migrates `[]byte(string(b))` back to `b`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof ArrayTypeExpr) {
                    ArrayTypeExpr arr = (ArrayTypeExpr) c.getFun();
                    if (arr.getElt() instanceof Ident && "byte".equals(((Ident) arr.getElt()).getName()) && arr.getLen() == null) {
                        if (c.getArgs() != null && c.getArgs().size() == 1) {
                            Expr arg = c.getArgs().get(0);
                            if (arg instanceof CallExpr) {
                                CallExpr strCast = (CallExpr) arg;
                                if (strCast.getFun() instanceof Ident && "string".equals(((Ident) strCast.getFun()).getName())) {
                                    if (strCast.getArgs() != null && strCast.getArgs().size() == 1) {
                                        return strCast.getArgs().get(0).withPrefix(c.getPrefix());
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
