package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class SortSortStringSlice extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use sort.Strings instead of sort.Sort(sort.StringSlice)";
    }

    @Override
    public String getDescription() {
        return "Migrates `sort.Sort(sort.StringSlice(x))` to `sort.Strings(x)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "sort".equals(((Ident) sel.getX()).getName()) && "Sort".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof CallExpr) {
                            CallExpr argCall = (CallExpr) arg;
                            if (argCall.getFun() instanceof SelectorExpr) {
                                SelectorExpr argSel = (SelectorExpr) argCall.getFun();
                                if (argSel.getX() instanceof Ident && "sort".equals(((Ident) argSel.getX()).getName()) && "StringSlice".equals(argSel.getSel().getName())) {
                                    
                                    Ident newIdent = sel.getSel().withName("Strings");
                                    return c.withFun(sel.withSel(newIdent)).withArgs(argCall.getArgs());
                                }
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
