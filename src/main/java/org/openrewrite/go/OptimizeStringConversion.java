package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class OptimizeStringConversion extends Recipe {

    @Override
    public String getDisplayName() {
        return "Optimize string conversion";
    }

    @Override
    public String getDescription() {
        return "Converts w.Write([]byte(s)) to io.WriteString(w, s) to avoid unnecessary allocations.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);
                
                // Look for w.Write(...)
                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if ("Write".equals(sel.getSel().getName()) && c.getArgs() != null && c.getArgs().size() == 1) {
                        Expr arg = c.getArgs().get(0);
                        
                        // Look for []byte(s) -> which is a CallExpr with fun = SliceTypeExpr and 1 arg
                        if (arg instanceof CallExpr) {
                            CallExpr typeCast = (CallExpr) arg;
                            if (typeCast.getFun() instanceof SliceTypeExpr) {
                                SliceTypeExpr sliceType = (SliceTypeExpr) typeCast.getFun();
                                if (sliceType.getElt() instanceof Ident && "byte".equals(((Ident) sliceType.getElt()).getName())) {
                                    
                                    if (typeCast.getArgs() != null && typeCast.getArgs().size() == 1) {
                                        Expr stringArg = typeCast.getArgs().get(0);
                                        
                                        // Create io.WriteString(w, s)
                                        Ident ioIdent = new Ident(Tree.randomId(), c.getFun().getPrefix(), Markers.EMPTY, "io", null);
                                        Ident writeStringIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "WriteString", null);
                                        SelectorExpr ioWriteString = new SelectorExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, ioIdent, writeStringIdent, null);
                                        
                                        return c.withFun(ioWriteString).withArgs(Arrays.asList(
                                                sel.getX().withPrefix(Space.EMPTY),
                                                stringArg.withPrefix(Space.build(" "))
                                        ));
                                    }
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
