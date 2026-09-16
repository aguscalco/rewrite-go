package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class FmtSprintfString extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove unnecessary fmt.Sprintf";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Sprintf(\"%s\", \"hello\")` to simply `\"hello\"`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName()) && "Sprintf".equals(sel.getSel().getName())) {
                        
                        Expr formatArg = c.getArgs().get(0);
                        Expr valArg = c.getArgs().get(1);
                        
                        if (formatArg instanceof BasicLit && valArg instanceof BasicLit) {
                            BasicLit formatLit = (BasicLit) formatArg;
                            BasicLit valLit = (BasicLit) valArg;
                            
                            if ("\"%s\"".equals(formatLit.getValue()) && "STRING".equals(valLit.getKind())) {
                                return valLit.withPrefix(c.getPrefix());
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
