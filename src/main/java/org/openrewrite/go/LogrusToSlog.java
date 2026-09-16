package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class LogrusToSlog extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate sirupsen/logrus to log/slog";
    }

    @Override
    public String getDescription() {
        return "Migrates `logrus.Info()`, `logrus.Warn()`, etc. to the Go 1.21+ standard library `slog` package.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof Ident && "logrus".equals(((Ident) sel.getX()).getName())) {
                        String method = sel.getSel().getName();
                        
                        // Map logrus methods to slog methods
                        String slogMethod = null;
                        switch (method) {
                            case "Info":
                            case "Infof":
                            case "Infoln":
                                slogMethod = "Info";
                                break;
                            case "Warn":
                            case "Warnf":
                            case "Warnln":
                            case "Warning":
                                slogMethod = "Warn";
                                break;
                            case "Error":
                            case "Errorf":
                            case "Errorln":
                                slogMethod = "Error";
                                break;
                            case "Debug":
                            case "Debugf":
                            case "Debugln":
                                slogMethod = "Debug";
                                break;
                            case "Fatal":
                            case "Fatalf":
                            case "Fatalln":
                            case "Panic":
                            case "Panicf":
                            case "Panicln":
                                // slog doesn't have Fatal/Panic natively that call os.Exit, but this is a structural migration.
                                // It's common to just map to Error and then manually handle the exit, or keep it as Error.
                                // We will map to Error.
                                slogMethod = "Error";
                                break;
                        }
                        
                        if (slogMethod != null) {
                            Ident slogIdent = ((Ident) sel.getX()).withName("slog");
                            Ident methodIdent = sel.getSel().withName(slogMethod);
                            return c.withFun(sel.withX(slogIdent).withSel(methodIdent));
                        }
                    }
                }

                return c;
            }
        };
    }
}
