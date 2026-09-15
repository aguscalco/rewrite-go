package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

public class UseNamedReturns extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use named returns";
    }

    @Override
    public String getDescription() {
        return "Flags functions with multiple anonymous return types for conversion to named returns.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public FuncDecl visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getType() != null && f.getType().getResults() != null) {
                    java.util.List<Field> results = f.getType().getResults();
                    if (results != null && results.size() > 1) {
                        // Check if they are unnamed
                        boolean allUnnamed = true;
                        for (Field field : results) {
                            if (field.getNames() != null && !field.getNames().isEmpty()) {
                                allUnnamed = false;
                                break;
                            }
                        }
                        
                        if (allUnnamed) {
                            return SearchResult.found(f, "Code Quality: Consider using named returns for functions with multiple return values");
                        }
                    }
                }
                
                return f;
            }
        };
    }
}
