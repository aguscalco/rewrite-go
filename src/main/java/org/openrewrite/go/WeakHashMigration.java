package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class WeakHashMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate weak hashes to SHA-256";
    }

    @Override
    public String getDescription() {
        return "Migrates deprecated and insecure `crypto/md5` and `crypto/sha1` hash functions to `crypto/sha256`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident) {
                        String pkg = ((Ident) sel.getX()).getName();
                        
                        if (("md5".equals(pkg) || "sha1".equals(pkg)) && "New".equals(sel.getSel().getName())) {
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
