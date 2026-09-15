package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ErrorVariableNaming extends Recipe {

    @Override
    public String getDisplayName() {
        return "Standardize error variable names";
    }

    @Override
    public String getDescription() {
        return "Ensures that exported error variables use the 'Err' prefix convention (e.g., ErrNotFound instead of errNotFound).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public GenDecl visitGenDecl(GenDecl genDecl, ExecutionContext ctx) {
                GenDecl g = (GenDecl) super.visitGenDecl(genDecl, ctx);
                
                // Only care about variable declarations
                if (!"var".equals(g.getTok().toLowerCase())) {
                    return g;
                }
                
                if (g.getSpecs() != null) {
                    List<Spec> newSpecs = new ArrayList<>(g.getSpecs());
                    boolean changed = false;
                    
                    for (int i = 0; i < newSpecs.size(); i++) {
                        Spec spec = newSpecs.get(i);
                        if (spec instanceof ValueSpec) {
                            ValueSpec v = (ValueSpec) spec;
                            
                            // Check if it's an error type or initialized with errors.New / fmt.Errorf
                            boolean isError = false;
                            
                            if (v.getType() instanceof Ident && "error".equals(((Ident) v.getType()).getName())) {
                                isError = true;
                            } else if (v.getValues() != null && !v.getValues().isEmpty()) {
                                Expr val = v.getValues().get(0);
                                if (val instanceof CallExpr) {
                                    Expr fun = ((CallExpr) val).getFun();
                                    if (fun instanceof SelectorExpr) {
                                        SelectorExpr sel = (SelectorExpr) fun;
                                        if (sel.getX() instanceof Ident) {
                                            String pkg = ((Ident) sel.getX()).getName();
                                            String func = sel.getSel().getName();
                                            if (("errors".equals(pkg) && "New".equals(func)) || 
                                                ("fmt".equals(pkg) && "Errorf".equals(func))) {
                                                isError = true;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (isError && v.getNames() != null && !v.getNames().isEmpty()) {
                                List<Ident> newNames = new ArrayList<>(v.getNames());
                                boolean specChanged = false;
                                
                                for (int j = 0; j < newNames.size(); j++) {
                                    Ident nameIdent = newNames.get(j);
                                    String name = nameIdent.getName();
                                    
                                    if (name.startsWith("err") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
                                        // It's errXxx, convert to ErrXxx
                                        String newName = "Err" + name.substring(3);
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
        };
    }
}
