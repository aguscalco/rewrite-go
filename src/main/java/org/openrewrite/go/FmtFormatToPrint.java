package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class FmtFormatToPrint extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use Print over Printf when no formatting is used";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Printf`, `fmt.Sprintf`, and `fmt.Fprintf` to their non-formatting equivalents if the format string contains no `%` verbs.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName())) {
                        String method = sel.getSel().getName();
                        
                        int formatArgIdx = -1;
                        String newMethod = null;
                        
                        if ("Printf".equals(method) && c.getArgs().size() == 1) {
                            formatArgIdx = 0;
                            newMethod = "Print";
                        } else if ("Sprintf".equals(method) && c.getArgs().size() == 1) {
                            formatArgIdx = 0;
                            newMethod = "Sprint";
                        } else if ("Fprintf".equals(method) && c.getArgs().size() == 2) {
                            formatArgIdx = 1;
                            newMethod = "Fprint";
                        }
                        
                        if (formatArgIdx >= 0 && newMethod != null) {
                            Expr formatArg = c.getArgs().get(formatArgIdx);
                            if (formatArg instanceof BasicLit && "STRING".equals(((BasicLit) formatArg).getKind())) {
                                String val = ((BasicLit) formatArg).getValue();
                                if (val != null && !val.contains("%")) {
                                    return c.withFun(sel.withSel(sel.getSel().withName(newMethod)));
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
