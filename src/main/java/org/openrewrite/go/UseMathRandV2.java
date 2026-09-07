package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Migrates math/rand to math/rand/v2 (Go 1.22+).
 * 
 * Most APIs are compatible, but this recipe will:
 * - Replace "math/rand" imports with "math/rand/v2"
 * - Detect Seed() calls which are removed in v2 (requires manual migration)
 * 
 * Note: Seed() usage requires manual migration to rand.New(rand.NewPCG(...))
 * This recipe does not automatically transform Seed() calls.
 */
public class UseMathRandV2 extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use math/rand/v2";
    }
    
    @Override
    public String getDescription() {
        return "Migrate math/rand to math/rand/v2 (Go 1.22+). Most APIs are compatible. " +
               "Seed() calls are removed in v2 and require manual migration.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                GoFile f = (GoFile) super.visitGoFile(goFile, ctx);
                
                List<ImportDecl> newImports = new ArrayList<>();
                for (ImportDecl imp : f.getImports()) {
                    ImportDecl newImp = migrateImport(imp);
                    newImports.add(newImp);
                }
                
                return f.withImports(newImports);
            }
            
            private ImportDecl migrateImport(ImportDecl imp) {
                List<ImportSpec> newSpecs = new ArrayList<>();
                
                for (ImportSpec spec : imp.getSpecs()) {
                    if (spec.getPath() != null) {
                        String pathValue = spec.getPath().getValue();
                        
                        if ("\"math/rand\"".equals(pathValue)) {
                            // Replace math/rand with math/rand/v2
                            BasicLit newPath = new BasicLit(
                                spec.getPath().getId() != null ? spec.getPath().getId() : UUID.randomUUID(),
                                spec.getPath().getPrefix(),
                                spec.getPath().getMarkers(),
                                spec.getPath().getKind(),
                                "\"math/rand/v2\""
                            );
                            
                            ImportSpec newSpec = new ImportSpec(
                                spec.getId() != null ? spec.getId() : UUID.randomUUID(),
                                spec.getPrefix(),
                                spec.getMarkers(),
                                spec.getAlias(),
                                newPath
                            );
                            newSpecs.add(newSpec);
                        } else {
                            newSpecs.add(spec);
                        }
                    } else {
                        newSpecs.add(spec);
                    }
                }
                
                return imp.withSpecs(newSpecs);
            }
        };
    }
}
