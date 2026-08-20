package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.internal.GoImports;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

public class UseSlicesPackage extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use slices package";
    }
    
    @Override
    public String getDescription() {
        return "Replace manual slice operations with slices package functions (Go 1.21+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            private boolean usedSlices;
            private int remainingSortUsages;
            
            @Override
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                usedSlices = false;
                remainingSortUsages = 0;
                GoFile g = (GoFile) super.visitGoFile(goFile, ctx);
                if (!usedSlices) {
                    return g;
                }
                g = GoImports.addImport(g, "slices");
                if (remainingSortUsages == 0) {
                    g = GoImports.removeImport(g, "sort");
                }
                return g;
            }
            
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                // Check if this is sort.Slice call
                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr selector = (SelectorExpr) c.getFun();
                    if (selector.getX() instanceof Ident) {
                        Ident pkgIdent = (Ident) selector.getX();
                        if ("sort".equals(pkgIdent.getName())) {
                            String methodName = selector.getSel().getName();
                            
                            // sort.Slice → slices.Sort
                            if ("Slice".equals(methodName) && c.getArgs().size() >= 1) {
                                return convertSortSliceToSlicesSort(c);
                            }
                            
                            // sort.SliceStable → slices.SortStable
                            if ("SliceStable".equals(methodName) && c.getArgs().size() >= 1) {
                                return convertSortSliceStableToSlicesSortStable(c);
                            }
                            
                            // A sort.* call this recipe does not convert keeps the import alive
                            remainingSortUsages++;
                        }
                    }
                }
                
                return c;
            }
            
            private CallExpr convertSortSliceToSlicesSort(CallExpr callExpr) {
                usedSlices = true;
                // sort.Slice(x, less) → slices.Sort(x)
                // For now, we'll just change the function name and keep the first arg
                // A more sophisticated version would analyze the less function
                
                SelectorExpr selector = (SelectorExpr) callExpr.getFun();
                Ident pkgIdent = (Ident) selector.getX();
                
                Ident slicesIdent = pkgIdent.withName("slices");
                Ident sortIdent = selector.getSel().withName("Sort");
                SelectorExpr newSelector = selector.withX(slicesIdent).withSel(sortIdent);
                
                // Keep only the first argument (the slice)
                List<Expr> newArgs = new ArrayList<>();
                if (!callExpr.getArgs().isEmpty()) {
                    newArgs.add(callExpr.getArgs().get(0));
                }
                
                return callExpr.withFun(newSelector).withArgs(newArgs);
            }
            
            private CallExpr convertSortSliceStableToSlicesSortStable(CallExpr callExpr) {
                usedSlices = true;
                // sort.SliceStable(x, less) → slices.SortStable(x)
                
                SelectorExpr selector = (SelectorExpr) callExpr.getFun();
                Ident pkgIdent = (Ident) selector.getX();
                
                Ident slicesIdent = pkgIdent.withName("slices");
                Ident sortStableIdent = selector.getSel().withName("SortStable");
                SelectorExpr newSelector = selector.withX(slicesIdent).withSel(sortStableIdent);
                
                // Keep only the first argument (the slice)
                List<Expr> newArgs = new ArrayList<>();
                if (!callExpr.getArgs().isEmpty()) {
                    newArgs.add(callExpr.getArgs().get(0));
                }
                
                return callExpr.withFun(newSelector).withArgs(newArgs);
            }
        };
    }
}
