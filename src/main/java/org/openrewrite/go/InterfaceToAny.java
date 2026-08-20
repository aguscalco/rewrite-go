package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class InterfaceToAny extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Replace interface{} with any";
    }
    
    @Override
    public String getDescription() {
        return "Replace empty interface{} with any type alias (Go 1.18+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitInterfaceTypeExpr(InterfaceTypeExpr e, ExecutionContext ctx) {
                InterfaceTypeExpr interfaceExpr = (InterfaceTypeExpr) super.visitInterfaceTypeExpr(e, ctx);
                if (interfaceExpr.getMethods() == null || interfaceExpr.getMethods().isEmpty()) {
                    return new Ident(
                        Tree.randomId(),
                        interfaceExpr.getPrefix(),
                        interfaceExpr.getMarkers(),
                        "any",
                        null
                    );
                }
                return interfaceExpr;
            }
        };
    }
}
