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
public class MockGeneration extends Recipe {

    @Option(displayName = "Interface name",
            description = "The name of the interface to generate a mock for.",
            example = "Database")
    String interfaceName;

    @Override
    public String getDisplayName() {
        return "Generate mock";
    }

    @Override
    public String getDescription() {
        return "Generates a basic mock struct implementation for a specified interface.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                GoFile f = (GoFile) super.visitGoFile(goFile, ctx);
                
                // Avoid generating if it already exists
                boolean exists = false;
                if (f.getDeclarations() != null) {
                    for (Decl decl : f.getDeclarations()) {
                        if (decl instanceof GenDecl) {
                            GenDecl gd = (GenDecl) decl;
                            if (gd.getSpecs() != null) {
                                for (Spec spec : gd.getSpecs()) {
                                    if (spec instanceof TypeSpec) {
                                        TypeSpec ts = (TypeSpec) spec;
                                        if (ts.getName() != null && ("Mock" + interfaceName).equals(ts.getName().getName())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (exists) {
                    return f;
                }
                
                // type MockX struct {}
                Ident nameIdent = new Ident(Tree.randomId(), Space.build(" "), Markers.EMPTY, "Mock" + interfaceName, null);
                StructTypeExpr structType = new StructTypeExpr(Tree.randomId(), Space.build(" "), Markers.EMPTY, Collections.emptyList());
                TypeSpec typeSpec = new TypeSpec(Tree.randomId(), Space.build("\n\n"), Markers.EMPTY, nameIdent, Collections.emptyList(), structType, false);
                
                GenDecl genDecl = new GenDecl(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "type", Collections.singletonList(typeSpec), false, Space.EMPTY);
                
                List<Decl> newDecls = new java.util.ArrayList<>(f.getDeclarations() == null ? Collections.emptyList() : f.getDeclarations());
                newDecls.add(genDecl);
                
                return f.withDeclarations(newDecls);
            }
        };
    }
}
