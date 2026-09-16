package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class DetectContextLeak extends Recipe {

    @Override
    public String getDisplayName() {
        return "Detect context leaks";
    }

    @Override
    public String getDescription() {
        return "Ensures that context.WithCancel, context.WithTimeout, and context.WithDeadline calls are followed by a deferred call to the returned cancel function to prevent memory leaks.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public BlockStmt visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);

                if (b.getStmts() != null) {
                    List<Stmt> newStmts = new ArrayList<>(b.getStmts());
                    boolean changed = false;

                    for (int i = 0; i < newStmts.size(); i++) {
                        Stmt stmt = newStmts.get(i);
                        
                        if (stmt instanceof AssignStmt) {
                            AssignStmt assign = (AssignStmt) stmt;
                            
                            // Look for context.With... calls on the RHS
                            if (assign.getRhs() != null && assign.getRhs().size() == 1 && assign.getRhs().get(0) instanceof CallExpr) {
                                CallExpr call = (CallExpr) assign.getRhs().get(0);
                                if (call.getFun() instanceof SelectorExpr) {
                                    SelectorExpr sel = (SelectorExpr) call.getFun();
                                    if (sel.getX() instanceof Ident && "context".equals(((Ident) sel.getX()).getName())) {
                                        String methodName = sel.getSel().getName();
                                        if ("WithCancel".equals(methodName) || "WithTimeout".equals(methodName) || "WithDeadline".equals(methodName)) {
                                            
                                            // Ensure there's a second variable returned (the cancel func)
                                            if (assign.getLhs() != null && assign.getLhs().size() == 2) {
                                                Expr cancelVar = assign.getLhs().get(1);
                                                if (cancelVar instanceof Ident) {
                                                    String cancelName = ((Ident) cancelVar).getName();
                                                    
                                                    // Check the rest of the block for a defer statement calling cancelName
                                                    boolean foundDefer = false;
                                                    for (Stmt subsequent : newStmts) {
                                                        if (subsequent instanceof DeferStmt) {
                                                            Expr callExpr = ((DeferStmt) subsequent).getCall();
                                                            if (callExpr instanceof CallExpr) {
                                                                CallExpr deferCall = (CallExpr) callExpr;
                                                                if (deferCall.getFun() instanceof Ident && cancelName.equals(((Ident) deferCall.getFun()).getName())) {
                                                                    foundDefer = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    
                                                    if (!foundDefer) {
                                                        newStmts.set(i, SearchResult.found(assign, "Context leak warning: The cancel function '" + cancelName + "' returned by " + methodName + " must be called, usually via defer, to avoid leaking the context."));
                                                        changed = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (changed) {
                        return b.withStmts(newStmts);
                    }
                }

                return b;
            }
        };
    }
}
