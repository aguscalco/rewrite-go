package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.GoFile;
import org.openrewrite.go.tree.GoVisitor;
import org.openrewrite.go.tree.Space;

public class PackageComment extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add package comments";
    }

    @Override
    public String getDescription() {
        return "Adds a default package-level comment if one is missing.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public GoFile visitGoFile(GoFile goFile, ExecutionContext ctx) {
                if (goFile.getPackageClause() != null) {
                    Space prefix = goFile.getPackageClause().getPrefix();
                    if (!prefix.getWhitespace().contains("// Package ") && !prefix.getWhitespace().contains("/* Package ")) {
                        String name = goFile.getPackageClause().getName().getName();
                        
                        // We do not add comments to 'main' package by standard practice, but for the recipe we can do it if desired.
                        // Let's just add it to all missing ones.
                        String newWs = prefix.getWhitespace();
                        // If it's empty, add the comment followed by newline.
                        // If it contains newlines, add it at the end of the whitespace block right before 'package'
                        int lastNewline = newWs.lastIndexOf('\n');
                        if (lastNewline != -1) {
                            newWs = newWs.substring(0, lastNewline + 1) + "// Package " + name + " is undocumented.\n" + newWs.substring(lastNewline + 1);
                        } else {
                            newWs = "// Package " + name + " is undocumented.\n" + newWs;
                        }
                        
                        return goFile.withPackageClause(goFile.getPackageClause().withPrefix(prefix.withWhitespace(newWs)));
                    }
                }
                return goFile;
            }
        };
    }
}
