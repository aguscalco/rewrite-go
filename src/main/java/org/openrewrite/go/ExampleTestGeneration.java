package org.openrewrite.go;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class ExampleTestGeneration extends Recipe {

    @Option(displayName = "Function name",
            description = "The name of the function to generate an example test for.",
            example = "Process")
    String functionName;

    @Override
    public String getDisplayName() {
        return "Generate example test";
    }

    @Override
    public String getDescription() {
        return "Generates a boilerplate Example function for the specified target function.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                GoFile f = (GoFile) super.visitGoFile(goFile, ctx);
                
                // Only act on test files
                if (!f.getSourcePath().toString().endsWith("_test.go")) {
                    return f;
                }
                
                // Avoid generating if it already exists
                boolean exists = false;
                if (f.getDeclarations() != null) {
                    for (Decl decl : f.getDeclarations()) {
                        if (decl instanceof FuncDecl) {
                            FuncDecl fd = (FuncDecl) decl;
                            if (fd.getName() != null && ("Example" + functionName).equals(fd.getName().getName())) {
                                exists = true;
                                break;
                            }
                        }
                    }
                }
                
                if (exists) {
                    return f;
                }
                
                // Generate func ExampleX() { ... }
                Ident nameIdent = new Ident(Tree.randomId(), Space.build("\n\n"), Markers.EMPTY, "Example" + functionName, null);
                FuncType funcType = new FuncType(Tree.randomId(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), null, null);
                
                // Add a comment to the body
                BlockStmt body = new BlockStmt(Tree.randomId(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Space.build("\n\t// Output:\n\t// \n"));
                FuncDecl exampleDecl = new FuncDecl(Tree.randomId(), Space.EMPTY, Markers.EMPTY, null, nameIdent, null, funcType, body);
                
                List<Decl> newDecls = new java.util.ArrayList<>(f.getDeclarations() == null ? Collections.emptyList() : f.getDeclarations());
                newDecls.add(exampleDecl);
                
                return f.withDeclarations(newDecls);
            }
        };
    }
}
