package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class GormV1ToV2 extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate GORM v1 to v2";
    }

    @Override
    public String getDescription() {
        return "Migrates GORM framework imports from github.com/jinzhu/gorm to gorm.io/gorm.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public ImportSpec visitImportSpec(ImportSpec importSpec, ExecutionContext ctx) {
                ImportSpec spec = (ImportSpec) super.visitImportSpec(importSpec, ctx);
                
                if (spec.getPath() != null) {
                    String path = spec.getPath().getValue();
                    if (path.contains("github.com/jinzhu/gorm")) {
                        String newPath = path.replace("github.com/jinzhu/gorm", "gorm.io/gorm");
                        return spec.withPath(spec.getPath().withValue(newPath));
                    }
                }
                
                return spec;
            }
        };
    }
}
