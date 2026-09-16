package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class InsecureTLSCheck extends Recipe {

    @Override
    public String getDisplayName() {
        return "Disable InsecureSkipVerify in TLS configurations";
    }

    @Override
    public String getDescription() {
        return "Finds and disables `InsecureSkipVerify: true` in `tls.Config`, which bypasses certificate verification and makes the application vulnerable to Man-In-The-Middle (MITM) attacks.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public KeyValueExpr visitKeyValueExpr(KeyValueExpr keyValueExpr, ExecutionContext ctx) {
                KeyValueExpr kv = (KeyValueExpr) super.visitKeyValueExpr(keyValueExpr, ctx);

                if (kv.getKey() instanceof Ident && "InsecureSkipVerify".equals(((Ident) kv.getKey()).getName())) {
                    if (kv.getValue() instanceof Ident && "true".equals(((Ident) kv.getValue()).getName())) {
                        // Change true to false
                        Ident falseIdent = ((Ident) kv.getValue()).withName("false");
                        return kv.withValue(falseIdent);
                    }
                }

                return kv;
            }
        };
    }
}
