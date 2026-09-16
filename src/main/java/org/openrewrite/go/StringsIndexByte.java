package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class StringsIndexByte extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use strings.IndexByte for single-character strings";
    }

    @Override
    public String getDescription() {
        return "Migrates `strings.Index(s, \"A\")` to `strings.IndexByte(s, 'A')` for better performance when searching for a single character.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() == 2) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "strings".equals(((Ident) sel.getX()).getName()) && "Index".equals(sel.getSel().getName())) {
                        
                        Expr subArg = c.getArgs().get(1);
                        if (subArg instanceof BasicLit && "STRING".equals(((BasicLit) subArg).getKind())) {
                            String val = ((BasicLit) subArg).getValue();
                            // If it is exactly a 1-character string like "A"
                            if (val != null && val.length() == 3 && val.startsWith("\"") && val.endsWith("\"")) {
                                char c1 = val.charAt(1);
                                if (c1 != '\\') { // skip escapes for simplicity
                                    Ident indexByteIdent = sel.getSel().withName("IndexByte");
                                    BasicLit runeLit = new BasicLit(UUID.randomUUID(), subArg.getPrefix(), Markers.EMPTY, "CHAR", "'" + c1 + "'");
                                    return c.withFun(sel.withSel(indexByteIdent)).withArgs(java.util.Arrays.asList(c.getArgs().get(0), runeLit));
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
