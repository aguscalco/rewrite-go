package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class GinV1ToV2 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate Gin v1 to v2";
    }

    @Override
    public String getDescription() {
        return "Migrates Gin framework imports from gopkg.in/gin-gonic/gin.v1 to github.com/gin-gonic/gin.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public ImportSpec visitImportSpec(ImportSpec importSpec, ExecutionContext ctx) {
                ImportSpec spec = (ImportSpec) super.visitImportSpec(importSpec, ctx);
                
                if (spec.getPath() != null) {
                    String path = spec.getPath().getValue();
                    if ("\"gopkg.in/gin-gonic/gin.v1\"".equals(path) || "`gopkg.in/gin-gonic/gin.v1`".equals(path)) {
                        String newPath = path.charAt(0) + "github.com/gin-gonic/gin" + path.charAt(path.length() - 1);
                        return spec.withPath(spec.getPath().withValue(newPath));
                    }
                }
                
                return spec;
            }
        };
    }
}
