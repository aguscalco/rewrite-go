package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.internal.GoImports;
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
            private boolean usedIo;
            private boolean usedOs;
            private boolean sawIoutil;
            
            @Override
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                usedIo = false;
                usedOs = false;
                sawIoutil = false;
                GoFile g = (GoFile) super.visitGoFile(goFile, ctx);
                if (!sawIoutil) {
                    return g;
                }
                // Replace the ioutil import with only the packages the rewritten call sites use.
                g = GoImports.removeImport(g, "io/ioutil");
                if (usedIo) {
                    g = GoImports.addImport(g, "io");
                }
                if (usedOs) {
                    g = GoImports.addImport(g, "os");
                }
                return g;
            }
            
            @Override
            public Tree visitSelectorExpr(SelectorExpr sel, ExecutionContext ctx) {
                SelectorExpr selectorExpr = (SelectorExpr) super.visitSelectorExpr(sel, ctx);
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
                    case "NopCloser":
                    case "Discard":
                        newPkg = "io";
                        break;
                    case "ReadFile":
                    case "WriteFile":
                    case "ReadDir":
                        newPkg = "os";
                        break;
                    case "TempDir":
                        newPkg = "os";
                        newMethod = "MkdirTemp";
                        break;
                    case "TempFile":
                        newPkg = "os";
                        newMethod = "CreateTemp";
                        break;
                    default:
                        return selectorExpr;
                }
                
                sawIoutil = true;
                if ("io".equals(newPkg)) {
                    usedIo = true;
                } else {
                    usedOs = true;
                }
                
                Ident newPkgIdent = pkgIdent.withName(newPkg);
                Ident newMethodIdent = selectorExpr.getSel().withName(newMethod);
                
                return selectorExpr.withX(newPkgIdent).withSel(newMethodIdent);
            }
        };
    }
}
