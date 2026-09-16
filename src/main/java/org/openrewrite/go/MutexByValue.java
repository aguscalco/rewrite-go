package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class MutexByValue extends Recipe {

    @Override
    public String getDisplayName() {
        return "Pass sync.Mutex by reference";
    }

    @Override
    public String getDescription() {
        return "Detects and fixes sync.Mutex or sync.RWMutex being passed by value in function signatures by changing them to pointers (*sync.Mutex).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Field visitField(Field field, ExecutionContext ctx) {
                Field f = (Field) super.visitField(field, ctx);

                if (f.getType() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) f.getType();
                    if (sel.getX() instanceof Ident && "sync".equals(((Ident) sel.getX()).getName())) {
                        String name = sel.getSel().getName();
                        if ("Mutex".equals(name) || "RWMutex".equals(name)) {
                            // Convert sync.Mutex to *sync.Mutex
                            StarExpr starExpr = new StarExpr(
                                UUID.randomUUID(),
                                sel.getPrefix(),
                                Markers.EMPTY,
                                sel.withPrefix(Space.EMPTY),
                                null
                            );
                            return f.withType(starExpr);
                        }
                    }
                }

                return f;
            }
        };
    }
}
