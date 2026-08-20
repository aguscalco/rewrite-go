package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Arrays;

public class MigrateToSlog extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Migrate to log/slog";
    }
    
    @Override
    public String getDescription() {
        return "Replace log.Printf with log/slog structured logging (Go 1.21+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
                CallExpr c = callExpr;
                
                // Check if this is log.Printf or log.Println
                if (c.getFun() instanceof SelectorExpr) {
                    SelectorExpr selector = (SelectorExpr) c.getFun();
                    if (selector.getX() instanceof Ident) {
                        Ident pkgIdent = (Ident) selector.getX();
                        if ("log".equals(pkgIdent.getName())) {
                            String methodName = selector.getSel().getName();
                            
                            // log.Printf → slog.Info
                            if ("Printf".equals(methodName)) {
                                return convertLogPrintfToSlogInfo(c);
                            }
                            
                            // log.Println → slog.Info
                            if ("Println".equals(methodName)) {
                                return convertLogPrintlnToSlogInfo(c);
                            }
                        }
                    }
                }
                
                return c;
            }
            
            private CallExpr convertLogPrintfToSlogInfo(CallExpr callExpr) {
                // log.Printf("message: %v", value) → slog.Info("message", "key", value)
                // For simplicity, we'll convert to slog.Info with the format string as message
                // A more sophisticated version would parse the format string
                
                Ident slogIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "slog",
                    null
                );
                
                Ident infoIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "Info",
                    null
                );
                
                SelectorExpr slogInfoSelector = new SelectorExpr(
                    Tree.randomId(),
                    callExpr.getPrefix(),
                    callExpr.getMarkers(),
                    slogIdent,
                    infoIdent,
                    null
                );
                
                // Keep all arguments for now
                // A real implementation would parse the format string and convert to key-value pairs
                return callExpr.withFun(slogInfoSelector);
            }
            
            private CallExpr convertLogPrintlnToSlogInfo(CallExpr callExpr) {
                // log.Println(args...) → slog.Info("message", args...)
                
                Ident slogIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "slog",
                    null
                );
                
                Ident infoIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    callExpr.getMarkers(),
                    "Info",
                    null
                );
                
                SelectorExpr slogInfoSelector = new SelectorExpr(
                    Tree.randomId(),
                    callExpr.getPrefix(),
                    callExpr.getMarkers(),
                    slogIdent,
                    infoIdent,
                    null
                );
                
                return callExpr.withFun(slogInfoSelector);
            }
        };
    }
}
