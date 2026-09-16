package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Collections;

public class FmtSprintfVToSprint extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use fmt.Sprint instead of fmt.Sprintf(\"%v\")";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Sprintf(\"%v\", x)` to `fmt.Sprint(x)`.";
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
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        if (arg1 instanceof BasicLit) {
                            BasicLit lit = (BasicLit) arg1;
                            if (lit.getKind().equals("STRING") && ("\"%v\"".equals(lit.getValue()) || "`%v`".equals(lit.getValue()))) {
                                Ident sprintIdent = sel.getSel().withName("Sprint");
                                return c.withFun(sel.withSel(sprintIdent)).withArgs(Collections.singletonList(arg2.withPrefix(Space.EMPTY)));
                            }
                        }
                    }
                }

                return c;
            }
        };
    }
}
