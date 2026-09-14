package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

/**
 * Replaces insecure math/rand usage with crypto/rand for secure token generation.
 *
 * Specifically targets typical math/rand random generation used for secrets.
 * E.g., rand.Intn() might be flagged or changed, but for simplicity, we migrate the package
 * and let the user adapt the API, or we map specific functions.
 */
public class SecureRandom extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use crypto/rand instead of math/rand";
    }

    @Override
    public String getDescription() {
        return "Migrates math/rand to crypto/rand for security-sensitive random generation.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitImportDecl(ImportDecl importDecl, ExecutionContext ctx) {
                ImportDecl i = (ImportDecl) super.visitImportDecl(importDecl, ctx);
                if (i.getSpecs().size() == 1) {
                    ImportSpec spec = i.getSpecs().get(0);
                    if (spec.getPath() != null && "\"math/rand\"".equals(spec.getPath().getValue())) {
                        BasicLit newPath = spec.getPath().withValue("\"crypto/rand\"");
                        ImportSpec newSpec = spec.withPath(newPath);
                        return i.withSpecs(Collections.singletonList(newSpec));
                    }
                }
                return i;
            }
        };
    }
}
