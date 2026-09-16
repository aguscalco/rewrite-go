package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class StringsIndexAnySingleChar extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.Index for single-character strings.IndexAny";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.IndexAny(s, \"a\")` to `strings.Index(s, \"a\")` for better performance when the character set has only one character.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName()) && "IndexAny".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(1);
                        if (arg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg;
                            if (lit.getKind().equals("STRING")) {
                                String val = lit.getValue();
                                
                                if (val.length() == 3) {
                                    Ident indexIdent = sel.getSel().withName("Index");
                                    return c.withFun(sel.withSel(indexIdent));
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
