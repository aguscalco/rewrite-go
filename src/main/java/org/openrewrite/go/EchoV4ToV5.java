package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class EchoV4ToV5 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate Echo v4 to v5";
    }

    @Override
    public String getDescription() {
        return "Migrates Echo framework imports from github.com/labstack/echo/v4 to github.com/labstack/echo/v5.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public ImportSpec visitImportSpec(ImportSpec importSpec, ExecutionContext ctx) {
                ImportSpec spec = (ImportSpec) super.visitImportSpec(importSpec, ctx);
                
                if (spec.getPath() != null) {
                    String path = spec.getPath().getValue();
                    if (path.contains("github.com/labstack/echo/v4")) {
                        String newPath = path.replace("github.com/labstack/echo/v4", "github.com/labstack/echo/v5");
                        return spec.withPath(spec.getPath().withValue(newPath));
                    }
                }
                
                return spec;
            }
        };
    }
}
