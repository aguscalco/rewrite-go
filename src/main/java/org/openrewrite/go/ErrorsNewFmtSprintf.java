package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ErrorsNewFmtSprintf extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use fmt.Errorf instead of errors.New(fmt.Sprintf())";
    }

    @Override
    public String getDescription() {
        return "Migrates `errors.New(fmt.Sprintf(format, args...))` to `fmt.Errorf(format, args...)`.";
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
                            CallExpr argCall = (CallExpr) arg;
                            if (argCall.getFun() instanceof SelectorExpr) {
                                SelectorExpr argSel = (SelectorExpr) argCall.getFun();
                                if (argSel.getX() instanceof Ident && "fmt".equals(((Ident) argSel.getX()).getName()) && "Sprintf".equals(argSel.getSel().getName())) {
                                    
                                    Ident fmtIdent = sel.getSel().withName("fmt");
                                    Ident errorfIdent = sel.getSel().withName("Errorf");
                                    return c.withFun(sel.withX(fmtIdent).withSel(errorfIdent)).withArgs(argCall.getArgs());
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
