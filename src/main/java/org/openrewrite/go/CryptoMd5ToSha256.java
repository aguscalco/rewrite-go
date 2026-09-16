package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class CryptoMd5ToSha256 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use crypto/sha256 instead of crypto/md5";
    }

    @Override
    public String getDescription() {
        return "Migrates `md5.New()` to `sha256.New()` to prevent weak hashing (gosec G401).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "md5".equals(((Ident) sel.getX()).getName())) {
                        
                        if ("New".equals(sel.getSel().getName())) {
                            Ident sha256Ident = ((Ident) sel.getX()).withName("sha256");
                            return c.withFun(sel.withX(sha256Ident));
                        }
                    }
                }

                return c;
            }
        };
    }
}
