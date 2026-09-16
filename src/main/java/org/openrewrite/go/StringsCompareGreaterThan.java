package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class StringsCompareGreaterThan extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use a > b instead of strings.Compare(a, b) > 0";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.Compare(a, b) > 0` to `a > b`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if (">".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue())) {
                            if (call.getFun() instanceof SelectorExpr && call.getArgs() != null && call.getArgs().size() == 2) {
                                SelectorExpr sel = (SelectorExpr) call.getFun();
                                if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName()) && "Compare".equals(sel.getSel().getName())) {
                                    
                                    Expr arg1 = call.getArgs().get(0);
                                    Expr arg2 = call.getArgs().get(1);
                                    
                                    return new BinaryExpr(UUID.randomUUID(), b.getPrefix(), Markers.EMPTY, arg1.withPrefix(Space.EMPTY), ">", arg2.withPrefix(Space.build(" ")), null);
                                }
                            }
                        }
                    }
                }

                return b;
            }
        };
    }
}
