package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

public class AddContextParameter extends Recipe {
    
    @Option(displayName = "Function name pattern",
            description = "Regex pattern to match function names that should have context added.",
            example = ".*",
            required = false)
    String functionNamePattern = ".*";
    
    @Override
    public String getDisplayName() {
        return "Add context.Context parameter";
    }
    
    @Override
    public String getDescription() {
        return "Add context.Context as the first parameter to functions matching the pattern.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                String funcName = funcDecl.getName().getName();
                
                if (!funcName.matches(functionNamePattern)) {
                    return funcDecl;
                }
                
                if (funcDecl.getType() == null || funcDecl.getType().getParams() == null || 
                    funcDecl.getType().getParams().isEmpty()) {
                    return funcDecl;
                }
                
                List<Field> params = funcDecl.getType().getParams();
                Field firstParam = params.get(0);
                
                if (firstParam.getType() != null && firstParam.getType() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) firstParam.getType();
                    if (sel.getX() instanceof Ident && 
                        ((Ident) sel.getX()).getName().equals("context") &&
                        sel.getSel().getName().equals("Context")) {
                        return funcDecl;
                    }
                }
                
                Ident contextIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    funcDecl.getMarkers(),
                    "context",
                    null
                );
                
                Ident contextTypeIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    funcDecl.getMarkers(),
                    "Context",
                    null
                );
                
                SelectorExpr contextSelector = new SelectorExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    funcDecl.getMarkers(),
                    contextIdent,
                    contextTypeIdent,
                    null
                );
                
                List<String> ctxNames = new ArrayList<>();
                ctxNames.add("ctx");
                
                Field ctxParam = new Field(
                    Tree.randomId(),
                    Space.EMPTY,
                    funcDecl.getMarkers(),
                    ctxNames,
                    contextSelector,
                    null
                );
                
                List<Field> newParams = new ArrayList<>();
                newParams.add(ctxParam);
                newParams.addAll(params);
                
                FuncType newType = funcDecl.getType().withParams(newParams);
                return funcDecl.withType(newType);
            }
        };
    }
}
