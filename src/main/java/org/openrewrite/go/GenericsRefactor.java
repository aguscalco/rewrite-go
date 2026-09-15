package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

import java.util.List;

public class GenericsRefactor extends Recipe {

    @Override
    public String getDisplayName() {
        return "Refactor interface{} to Generics";
    }

    @Override
    public String getDescription() {
        return "Flags functions accepting 'interface{}' or 'any' for potential migration to Go 1.18+ Type Parameters (Generics).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public FuncDecl visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getType() != null && f.getType().getParams() != null) {
                    List<Field> params = f.getType().getParams();
                    boolean hasAny = false;
                    
                    for (Field param : params) {
                        Expr type = param.getType();
                        if (type instanceof Ident && "any".equals(((Ident) type).getName())) {
                            hasAny = true;
                            break;
                        } else if (type instanceof InterfaceTypeExpr) {
                            InterfaceTypeExpr it = (InterfaceTypeExpr) type;
                            if (it.getMethods() == null || it.getMethods().isEmpty()) {
                                hasAny = true; // empty interface{}
                                break;
                            }
                        }
                    }
                    
                    if (hasAny) {
                        return SearchResult.found(f, "Go 1.18+: Consider migrating 'any' or 'interface{}' to a Type Parameter (Generics)");
                    }
                }
                
                return f;
            }
        };
    }
}
