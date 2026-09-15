package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ExportedComment extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add comments to exported symbols";
    }

    @Override
    public String getDescription() {
        return "Adds a default comment to exported functions, types, and variables that lack documentation.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public FuncDecl visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);
                
                String name = f.getName().getName();
                if (!name.isEmpty() && Character.isUpperCase(name.charAt(0))) {
                    Space prefix = f.getPrefix();
                    if (!hasComment(prefix, name)) {
                        f = f.withPrefix(addComment(prefix, name));
                    }
                }
                return f;
            }

            @Override
            public GenDecl visitGenDecl(GenDecl genDecl, ExecutionContext ctx) {
                GenDecl g = (GenDecl) super.visitGenDecl(genDecl, ctx);
                
                if (g.getSpecs() != null && !g.getSpecs().isEmpty()) {
                    Spec firstSpec = g.getSpecs().get(0);
                    String name = null;
                    if (firstSpec instanceof TypeSpec) {
                        name = ((TypeSpec) firstSpec).getName().getName();
                    } else if (firstSpec instanceof ValueSpec) {
                        if (!((ValueSpec) firstSpec).getNames().isEmpty()) {
                            name = ((ValueSpec) firstSpec).getNames().get(0).getName();
                        }
                    }
                    
                    if (name != null && !name.isEmpty() && Character.isUpperCase(name.charAt(0))) {
                        Space prefix = g.getPrefix();
                        if (!hasComment(prefix, name)) {
                            g = g.withPrefix(addComment(prefix, name));
                        }
                    }
                }
                return g;
            }

            private boolean hasComment(Space prefix, String name) {
                String ws = prefix.getWhitespace();
                return ws.contains("// " + name) || ws.contains("/* " + name);
            }
            
            private Space addComment(Space prefix, String name) {
                String ws = prefix.getWhitespace();
                String comment = "// " + name + " is undocumented.\n";
                int lastNewline = ws.lastIndexOf('\n');
                if (lastNewline != -1) {
                    return prefix.withWhitespace(ws.substring(0, lastNewline + 1) + comment + ws.substring(lastNewline + 1));
                } else {
                    return prefix.withWhitespace(comment + ws);
                }
            }
        };
    }
}
