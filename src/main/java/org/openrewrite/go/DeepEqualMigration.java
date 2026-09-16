package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class DeepEqualMigration extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate reflect.DeepEqual to cmp.Equal";
    }

    @Override
    public String getDescription() {
        return "Converts usage of reflect.DeepEqual to the safer and more robust cmp.Equal from github.com/google/go-cmp/cmp.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "reflect".equals(((Ident) sel.getX()).getName())) {
                        if ("DeepEqual".equals(sel.getSel().getName())) {
                            
                            Ident cmpIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "cmp", null);
                            Ident equalIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Equal", null);
                            SelectorExpr cmpEqualSel = new SelectorExpr(UUID.randomUUID(), c.getFun().getPrefix(), Markers.EMPTY, cmpIdent, equalIdent, null);
                            
                            return c.withFun(cmpEqualSel);
                        }
                    }
                }

                return c;
            }
        };
    }
}
