package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class FmtFprintToPrint extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use fmt.Print over fmt.Fprint(os.Stdout)";
    }

    @Override
    public String getDescription() {
        return "Migrates `fmt.Fprint(os.Stdout, ...)` to `fmt.Print(...)` and similarly for Fprintf/Fprintln.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr && c.getArgs() != null && c.getArgs().size() >= 1) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "fmt".equals(((Ident) sel.getX()).getName())) {
                        String name = sel.getSel().getName();
                        if ("Fprint".equals(name) || "Fprintf".equals(name) || "Fprintln".equals(name)) {
                            
                            Expr firstArg = c.getArgs().get(0);
                            if (isOsStdout(firstArg)) {
                                String newName = name.substring(1); // Remove 'F'
                                newName = Character.toUpperCase(newName.charAt(0)) + newName.substring(1);
                                Ident newIdent = sel.getSel().withName(newName);
                                
                                java.util.List<Expr> newArgs = new java.util.ArrayList<>(c.getArgs());
                                newArgs.remove(0); // Remove os.Stdout
                                
                                return c.withFun(sel.withSel(newIdent)).withArgs(newArgs);
                            }
                        }
                    }
                }

                return c;
            }

            private boolean isOsStdout(Expr expr) {
                if (expr instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) expr;
                    if (sel.getX() instanceof Ident && "os".equals(((Ident) sel.getX()).getName())) {
                        return "Stdout".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
