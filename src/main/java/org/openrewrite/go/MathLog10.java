package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MathLog10 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use math.Log10";
    }

    @Override
    public String getDescription() {
        return "Migrates `math.Log(x) / math.Log(10)` to `math.Log10(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("/".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof CallExpr) {
                        CallExpr xCall = (CallExpr) b.getX();
                        CallExpr yCall = (CallExpr) b.getY();
                        
                        if (isMathLog(xCall) && isMathLog(yCall)) {
                            Expr yArg = yCall.getArgs().get(0);
                            if (yArg instanceof BasicLit && "10".equals(((BasicLit) yArg).getValue())) {
                                
                                SelectorExpr sel = (SelectorExpr) xCall.getFun();
                                Ident log10Ident = sel.getSel().withName("Log10");
                                return xCall.withFun(sel.withSel(log10Ident)).withPrefix(b.getPrefix());
                            }
                        }
                    }
                }

                return b;
            }

            private boolean isMathLog(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "math".equals(((Ident) sel.getX()).getName())) {
                        return "Log".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
