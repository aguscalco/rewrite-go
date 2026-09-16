package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.UUID;

public class PreferFilepathJoin extends Recipe {

    @Override
    public String getDisplayName() {
        return "Prefer filepath.Join over string concatenation";
    }

    @Override
    public String getDescription() {
        return "Replaces string concatenation involving `\"/\"` with `filepath.Join` to ensure OS-agnostic path construction, matching gocritic's preferFilepathJoin.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("+".equals(b.getOp())) {
                    // Match (a + "/") + b
                    if (b.getX() instanceof BinaryExpr) {
                        BinaryExpr lhs = (BinaryExpr) b.getX();
                        if ("+".equals(lhs.getOp()) && lhs.getY() instanceof BasicLit) {
                            BasicLit middleLit = (BasicLit) lhs.getY();
                            if ("\"/\"".equals(middleLit.getValue())) {
                                
                                Expr p1 = lhs.getX();
                                Expr p2 = b.getY();
                                
                                // Create filepath.Join(p1, p2)
                                Ident filepathIdent = new Ident(UUID.randomUUID(), b.getPrefix(), Markers.EMPTY, "filepath", null);
                                Ident joinIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Join", null);
                                SelectorExpr sel = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, filepathIdent, joinIdent, null);
                                
                                CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, sel, Arrays.asList(p1.withPrefix(Space.EMPTY), p2.withPrefix(Space.build(" "))), false, null);
                                
                                return call;
                            }
                        }
                    }
                }

                return b;
            }
        };
    }
}
