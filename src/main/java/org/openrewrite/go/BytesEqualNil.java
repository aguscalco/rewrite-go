package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class BytesEqualNil extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use len(b) == 0 instead of bytes.Equal(b, nil)";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.Equal(b, nil)` or `bytes.Equal(b, []byte{})` to `len(b) == 0`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName()) && "Equal".equals(sel.getSel().getName())) {
                        
                        Expr arg1 = c.getArgs().get(0);
                        Expr arg2 = c.getArgs().get(1);
                        
                        if (isNilOrEmptyByteSlice(arg2)) {
                            return toLenZero(arg1, c.getPrefix());
                        } else if (isNilOrEmptyByteSlice(arg1)) {
                            return toLenZero(arg2, c.getPrefix());
                        }
                    }
                }

                return c;
            }

            private boolean isNilOrEmptyByteSlice(Expr expr) {
                if (expr instanceof Ident && "nil".equals(((Ident) expr).getName())) {
                    return true;
                }
                return false;
            }
            
            private Tree toLenZero(Expr arg, Space prefix) {
                Ident lenIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "len", null);
                CallExpr lenCall = new CallExpr(UUID.randomUUID(), prefix, Markers.EMPTY, lenIdent, Collections.singletonList(arg.withPrefix(Space.EMPTY)), false, null);
                BasicLit zeroLit = new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "0");
                
                return new BinaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, lenCall, "==", zeroLit, null);
            }
        };
    }
}
