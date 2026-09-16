package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class CryptoSha1SumToSha256 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use crypto/sha256 instead of crypto/sha1";
    }

    @Override
    public String getDescription() {
        return "Migrates `sha1.Sum(data)` to `sha256.Sum256(data)` to prevent weak hashing (gosec G401).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "sha1".equals(((Ident) sel.getX()).getName())) {
                        
                        if ("Sum".equals(sel.getSel().getName())) {
                            Ident sha256Ident = ((Ident) sel.getX()).withName("sha256");
                            Ident sum256Ident = sel.getSel().withName("Sum256");
                            return c.withFun(sel.withX(sha256Ident).withSel(sum256Ident));
                        }
                    }
                }

                return c;
            }
        };
    }
}
