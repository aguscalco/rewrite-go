package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class SprintfIntToItoa extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strconv.Itoa";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Sprintf(\"%d\", i)` to `strconv.Itoa(i)` for better performance.";
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
                        if (formatArg instanceof BasicLit && "\"%d\"".equals(((BasicLit) formatArg).getValue())) {
                            
                            Ident strconvIdent = new Ident(UUID.randomUUID(), sel.getX().getPrefix(), Markers.EMPTY, "strconv", null);
                            Ident itoaIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Itoa", null);
                            SelectorExpr newSel = new SelectorExpr(UUID.randomUUID(), sel.getPrefix(), Markers.EMPTY, strconvIdent, itoaIdent, null);
                            
                            return c.withFun(newSel).withArgs(java.util.Collections.singletonList(c.getArgs().get(1).withPrefix(Space.EMPTY)));
                        }
                    }
                }

                return c;
            }
        };
    }
}
