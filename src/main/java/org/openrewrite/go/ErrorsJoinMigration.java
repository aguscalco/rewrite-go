package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ErrorsJoinMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate multierror.Append to errors.Join";
    }

    @Override
    public String getDescription() {
        return "Migrates third-party multierror.Append calls (e.g. hashicorp/go-multierror) to the Go 1.20+ standard library errors.Join.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "multierror".equals(((Ident) sel.getX()).getName())) {
                        if ("Append".equals(sel.getSel().getName())) {
                            Ident errorsIdent = ((Ident) sel.getX()).withName("errors");
                            Ident joinIdent = sel.getSel().withName("Join");
                            
                            return c.withFun(sel.withX(errorsIdent).withSel(joinIdent));
                        }
                    }
                }
                
                return c;
            }
        };
    }
}
