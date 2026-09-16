package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class FmtErrorfToErrorsNew extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use errors.New for static errors";
    }

    @Override
    public String getDescription() {
        return "Replaces `fmt.Errorf(\"static string\")` with `errors.New(\"static string\")` for static error messages.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName()) && "Errorf".equals(sel.getSel().getName())) {
                        
                        Expr formatArg = c.getArgs().get(0);
                        if (formatArg instanceof BasicLit && "STRING".equals(((BasicLit) formatArg).getKind())) {
                            String val = ((BasicLit) formatArg).getValue();
                            // Make sure there are no formatting verbs
                            if (val != null && !val.contains("%")) {
                                Ident errorsIdent = ((Ident) sel.getX()).withName("errors");
                                Ident newIdent = sel.getSel().withName("New");
                                return c.withFun(sel.withX(errorsIdent).withSel(newIdent));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
