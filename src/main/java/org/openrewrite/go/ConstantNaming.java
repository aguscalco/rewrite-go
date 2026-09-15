package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ConstantNaming extends Recipe {

    @Override
    public String getDisplayName() {
        return "Standardize constant names";
    }

    @Override
    public String getDescription() {
        return "Converts SCREAMING_SNAKE_CASE constants to Go's idiomatic CamelCase.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public GenDecl visitGenDecl(GenDecl genDecl, ExecutionContext ctx) {
                GenDecl g = (GenDecl) super.visitGenDecl(genDecl, ctx);
                
                if (!"const".equals(g.getTok().toLowerCase())) {
                    return g;
                }
                
                if (g.getSpecs() != null) {
                    List<Spec> newSpecs = new ArrayList<>(g.getSpecs());
                    boolean changed = false;
                    
                    for (int i = 0; i < newSpecs.size(); i++) {
                        Spec spec = newSpecs.get(i);
                        if (spec instanceof ValueSpec) {
                            ValueSpec v = (ValueSpec) spec;
                            
                            if (v.getNames() != null && !v.getNames().isEmpty()) {
                                List<Ident> newNames = new ArrayList<>(v.getNames());
                                boolean specChanged = false;
                                
                                for (int j = 0; j < newNames.size(); j++) {
                                    Ident nameIdent = newNames.get(j);
                                    String name = nameIdent.getName();
                                    
                                    if (name.contains("_") && name.toUpperCase().equals(name)) {
                                        String newName = toCamelCase(name);
                                        newNames.set(j, nameIdent.withName(newName));
                                        specChanged = true;
                                    }
                                }
                                
                                if (specChanged) {
                                    newSpecs.set(i, v.withNames(newNames));
                                    changed = true;
                                }
                            }
                        }
                    }
                    
                    if (changed) {
                        return g.withSpecs(newSpecs);
                    }
                }
                
                return g;
            }
            
            private String toCamelCase(String snakeCase) {
                StringBuilder result = new StringBuilder();
                boolean capitalizeNext = true;
                for (char c : snakeCase.toCharArray()) {
                    if (c == '_') {
                        capitalizeNext = true;
                    } else if (capitalizeNext) {
                        result.append(Character.toUpperCase(c));
                        capitalizeNext = false;
                    } else {
                        result.append(Character.toLowerCase(c));
                    }
                }
                return result.toString();
            }
        };
    }
}
