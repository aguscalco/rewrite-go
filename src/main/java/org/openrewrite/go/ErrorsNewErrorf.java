package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ErrorsNewErrorf extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use fmt.Errorf instead of errors.New with fmt.Sprintf";
    }

    @Override
    public String getDescription() {
        return "Replaces `errors.New(fmt.Sprintf(...))` with `fmt.Errorf(...)` for better performance and readability.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "errors".equals(((Ident) sel.getX()).getName()) && "New".equals(sel.getSel().getName())) {
                        
                        Expr arg = c.getArgs().get(0);
                        if (arg instanceof CallExpr) {
                            CallExpr innerCall = (CallExpr) arg;
                            if (innerCall.getFun() instanceof SelectorExpr) {
                                SelectorExpr innerSel = (SelectorExpr) innerCall.getFun();
                                if (innerSel.getX() instanceof Ident && "fmt".equals(((Ident) innerSel.getX()).getName()) && "Sprintf".equals(innerSel.getSel().getName())) {
                                    
                                    Ident fmtIdent = ((Ident) sel.getX()).withName("fmt");
                                    Ident errorfIdent = sel.getSel().withName("Errorf");
                                    
                                    return c.withFun(sel.withX(fmtIdent).withSel(errorfIdent)).withArgs(innerCall.getArgs());
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
