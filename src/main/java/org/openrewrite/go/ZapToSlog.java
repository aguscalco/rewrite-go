package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ZapToSlog extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate go.uber.org/zap to log/slog";
    }

    @Override
    public String getDescription() {
        return "Migrates `zap.L().Info()` or `zap.S().Infof()` to the Go 1.21+ standard library `slog` package.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public CallExpr visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = (CallExpr) super.visitCallExpr(callExpr, ctx);

                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getFun();
                    if (sel.getX() instanceof CallExpr) {
                        CallExpr innerCall = (CallExpr) sel.getX();
                        if (innerCall.getFun() instanceof SelectorExpr) {
                            SelectorExpr innerSel = (SelectorExpr) innerCall.getFun();
                            if (innerSel.getX() instanceof Ident && "zap".equals(((Ident) innerSel.getX()).getName())) {
                                String loggerMethod = innerSel.getSel().getName();
                                if ("L".equals(loggerMethod) || "S".equals(loggerMethod)) {
                                    
                                    String method = sel.getSel().getName();
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
                                    }
                                    
                                    if (slogMethod != null) {
                                        Ident slogIdent = ((Ident) innerSel.getX()).withName("slog");
                                        Ident methodIdent = sel.getSel().withName(slogMethod);
                                        return c.withFun(sel.withX(slogIdent).withSel(methodIdent));
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
