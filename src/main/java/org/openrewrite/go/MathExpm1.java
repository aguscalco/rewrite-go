package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathExpm1 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Expm1";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Exp(x) - 1` to `math.Expm1(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("-".equals(b.getOp())) {
                    if (isMathExp(b.getX()) && isOne(b.getY())) {
                        CallExpr xCall = (CallExpr) b.getX();
                        SelectorExpr sel = (SelectorExpr) xCall.getFun();
                        Ident expm1Ident = sel.getSel().withName("Expm1");
                        return xCall.withFun(sel.withSel(expm1Ident)).withPrefix(b.getPrefix());
                    }
                }

                return b;
            }

            private boolean isMathExp(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 1) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                            return "Exp".equals(sel.getSel().getName());
                        }
                    }
                }
                return false;
            }
            
            private boolean isOne(Expr expr) {
                if (expr instanceof BasicLit) {
                    return "1".equals(((BasicLit) expr).getValue()) || "1.0".equals(((BasicLit) expr).getValue());
                }
                return false;
            }
        };
    }
}
