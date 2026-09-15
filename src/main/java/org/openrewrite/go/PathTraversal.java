package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Prevents path traversal vulnerabilities by wrapping unsafe file paths
 * in filepath.Clean() before they are used in os.Open or os.ReadFile.
 */
public class PathTraversal extends Recipe {

    @Override
    public String getDisplayName() {
        return "Sanitize file paths to prevent path traversal";
    }

    @Override
    public String getDescription() {
        return "Wraps file paths in filepath.Clean() before passing them to file system operations like os.Open.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (!(c.getFun() instanceof SelectorExpr)) {
                    return c;
                }

                SelectorExpr sel = (SelectorExpr) c.getFun();
                if (!(sel.getX() instanceof Ident)) {
                    return c;
                }

                Ident pkg = (Ident) sel.getX();
                String methodName = sel.getSel().getName();

                if ("os".equals(pkg.getName()) && 
                    ("Open".equals(methodName) || "ReadFile".equals(methodName) || "WriteFile".equals(methodName))) {
                    
                    if (c.getArgs().isEmpty()) {
                        return c;
                    }

                    Expr pathArg = c.getArgs().get(0);
                    
                    // Check if it's already wrapped in filepath.Clean
                    if (pathArg instanceof CallExpr) {
                        CallExpr argCall = (CallExpr) pathArg;
                        if (argCall.getFun() instanceof SelectorExpr) {
                            SelectorExpr argSel = (SelectorExpr) argCall.getFun();
                            if (argSel.getX() instanceof Ident && 
                                "filepath".equals(((Ident) argSel.getX()).getName()) &&
                                "Clean".equals(argSel.getSel().getName())) {
                                return c;
                            }
                        }
                    }

                    // Wrap the argument in filepath.Clean
                    Ident filepathPkg = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "filepath", null);
                    Ident cleanMethod = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "Clean", null);
                    SelectorExpr cleanSel = new SelectorExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, filepathPkg, cleanMethod, null);

                    Space origPrefix = pathArg.getPrefix();
                    Expr strippedArg = pathArg instanceof Go ? (Expr) ((Go) pathArg).withPrefix(Space.EMPTY) : pathArg;

                    CallExpr cleanCall = new CallExpr(
                            Tree.randomId(),
                            origPrefix,
                            Markers.EMPTY,
                            cleanSel,
                            Collections.singletonList(strippedArg),
                            false,
                            null
                    );

                    List<Expr> newArgs = new ArrayList<>(c.getArgs());
                    newArgs.set(0, cleanCall);
                    return c.withArgs(newArgs);
                }

                return c;
            }
        };
    }
}
