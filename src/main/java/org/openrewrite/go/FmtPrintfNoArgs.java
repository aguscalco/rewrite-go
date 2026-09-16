package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class FmtPrintfNoArgs extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use fmt.Print instead of fmt.Printf with no formatting";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Printf(\"literal string without formatting\")` to `fmt.Print`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName()) && "Printf".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg;
                            if (lit.getKind().equals("STRING")) {
                                String val = lit.getValue();
                                if (!val.contains("%")) {
                                    Ident printIdent = sel.getSel().withName("Print");
                                    return c.withFun(sel.withSel(printIdent));
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
