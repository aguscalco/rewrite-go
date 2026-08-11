package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MigrateIoutilToIO extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Migrate io/ioutil to io and os";
    }
    
    @Override
    public String getDescription() {
        return "Replace deprecated io/ioutil package with io and os equivalents (Go 1.16+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitImportSpec(ImportSpec importSpec, ExecutionContext ctx) {
                String path = importSpec.getPath().getValue();
                if (path.contains("io/ioutil")) {
                    BasicLit newPath = importSpec.getPath().withValue(path.replace("io/ioutil", "io"));
                    return importSpec.withPath(newPath);
                }
                return importSpec;
            }
            
            @Override
            public Tree visitSelectorExpr(SelectorExpr selectorExpr, ExecutionContext ctx) {
                if (!(selectorExpr.getX() instanceof Ident)) {
                    return selectorExpr;
                }
                
                Ident pkgIdent = (Ident) selectorExpr.getX();
                if (!"ioutil".equals(pkgIdent.getName())) {
                    return selectorExpr;
                }
                
                String methodName = selectorExpr.getSel().getName();
                String newPkg = "io";
                String newMethod = methodName;
                
                switch (methodName) {
                    case "ReadAll":
                    case "ReadFile":
                        newPkg = "io";
                        break;
                    case "WriteFile":
                        newPkg = "os";
                        break;
                    case "ReadDir":
                        newPkg = "os";
                        newMethod = "ReadDir";
                        break;
                    case "NopCloser":
                        newPkg = "io";
                        break;
                    case "Discard":
                        newPkg = "io";
                        break;
                    default:
                        return selectorExpr;
                }
                
                Ident newPkgIdent = pkgIdent.withName(newPkg);
                Ident newMethodIdent = selectorExpr.getSel().withName(newMethod);
                
                return selectorExpr.withX(newPkgIdent).withSel(newMethodIdent);
            }
        };
    }
}
