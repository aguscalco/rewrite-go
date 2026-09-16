package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MockgenToUberMock extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate golang/mock to uber/mock";
    }

    @Override
    public String getDescription() {
        return "Migrates deprecated github.com/golang/mock to the officially maintained go.uber.org/mock library.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public ImportSpec visitImportSpec(ImportSpec importSpec, ExecutionContext ctx) {
                ImportSpec spec = (ImportSpec) super.visitImportSpec(importSpec, ctx);

                if (spec.getPath() != null) {
                    String pathValue = spec.getPath().getValue();
                    if (pathValue.contains("github.com/golang/mock")) {
                        String newPath = pathValue.replace("github.com/golang/mock", "go.uber.org/mock");
                        return spec.withPath(spec.getPath().withValue(newPath));
                    }
                }

                return spec;
            }
        };
    }
}
