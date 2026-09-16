package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class WeakCryptoKeyCheck extends Recipe {

    @Override
    public String getDisplayName() {
        return "Enforce secure crypto key lengths";
    }

    @Override
    public String getDescription() {
        return "Upgrades weak RSA keys (e.g. 1024-bit) to industry standard 2048-bit keys when calling `rsa.GenerateKey`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "rsa".equals(((Ident) sel.getX()).getName())) {
                        if ("GenerateKey".equals(sel.getSel().getName()) && c.getArgs() != null && c.getArgs().size() == 2) {
                            Expr bitsArg = c.getArgs().get(1);
                            
                            if (bitsArg instanceof BasicLit) {
                                BasicLit bitsLit = (BasicLit) bitsArg;
                                if ("INT".equals(bitsLit.getKind()) && ("1024".equals(bitsLit.getValue()) || "512".equals(bitsLit.getValue()))) {
                                    // Replace with 2048
                                    return c.withArgs(java.util.Arrays.asList(c.getArgs().get(0), bitsLit.withValue("2048")));
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
