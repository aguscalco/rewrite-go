package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;
import java.util.UUID;

public class UseClearBuiltin extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use clear builtin";
    }

    @Override
    public String getDescription() {
        return "Migrates map-clearing range loops to the Go 1.21+ clear built-in function.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Stmt visitRangeStmt(RangeStmt rangeStmt, ExecutionContext ctx) {
                RangeStmt r = (RangeStmt) super.visitRangeStmt(rangeStmt, ctx);

                if (r.getValue() != null) {
                    return r;
                }

                if (r.getBody() != null && r.getBody().getStmts() != null && r.getBody().getStmts().size() == 1) {
                    Stmt stmt = r.getBody().getStmts().get(0);
                    if (stmt instanceof ExprStmt) {
                        Expr expr = ((ExprStmt) stmt).getExpr();
                        if (expr instanceof CallExpr) {
                            CallExpr call = (CallExpr) expr;
                            if (call.getFun() instanceof Ident && "delete".equals(((Ident) call.getFun()).getName())) {
                                if (call.getArgs() != null && call.getArgs().size() == 2) {
                                    Expr arg0 = call.getArgs().get(0);
                                    Expr arg1 = call.getArgs().get(1);

                                    // Simple exact match check for the Ident names
                                    if (arg0 instanceof Ident && r.getX() instanceof Ident && 
                                        arg1 instanceof Ident && r.getKey() instanceof Ident) {
                                        
                                        String mapName = ((Ident) r.getX()).getName();
                                        String keyName = ((Ident) r.getKey()).getName();
                                        
                                        if (mapName.equals(((Ident) arg0).getName()) && 
                                            keyName.equals(((Ident) arg1).getName())) {
                                            
                                            Ident clearIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "clear", null);
                                            CallExpr clearCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, clearIdent, Collections.singletonList(r.getX()), false, null);
                                            return new ExprStmt(UUID.randomUUID(), r.getPrefix(), Markers.EMPTY, clearCall);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return r;
            }
        };
    }
}
