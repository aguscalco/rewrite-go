package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converts map creations followed by a loop into preallocated maps.
 */
public class PreallocateMaps extends Recipe {

    @Override
    public String getDisplayName() {
        return "Preallocate maps";
    }

    @Override
    public String getDescription() {
        return "Converts map creations into preallocated maps when the capacity is determinable from a subsequent range loop.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);

                if (b.getStmts() == null || b.getStmts().size() < 2) {
                    return b;
                }

                List<Stmt> newStmts = new ArrayList<>(b.getStmts());
                boolean changed = false;

                for (int i = 0; i < newStmts.size() - 1; i++) {
                    Stmt s1 = newStmts.get(i);
                    Stmt s2 = newStmts.get(i + 1);

                    // Look for `m := make(map[K]V)`
                    if (s1 instanceof AssignStmt) {
                        AssignStmt assign = (AssignStmt) s1;
                        if (":=".equals(assign.getTok()) && assign.getLhs() != null && assign.getLhs().size() == 1 &&
                            assign.getRhs() != null && assign.getRhs().size() == 1) {
                            
                            if (assign.getLhs().get(0) instanceof Ident && assign.getRhs().get(0) instanceof CallExpr) {
                                String mapName = ((Ident) assign.getLhs().get(0)).getName();
                                CallExpr makeCall = (CallExpr) assign.getRhs().get(0);
                                
                                if (makeCall.getFun() instanceof Ident && "make".equals(((Ident) makeCall.getFun()).getName()) &&
                                    makeCall.getArgs() != null && makeCall.getArgs().size() == 1 &&
                                    makeCall.getArgs().get(0) instanceof MapTypeExpr) {
                                    
                                    // Look for `for _, x := range items { m[x] = ... }`
                                    if (s2 instanceof RangeStmt) {
                                        RangeStmt rangeStmt = (RangeStmt) s2;
                                        if (assignsToMap(rangeStmt, mapName)) {
                                            Expr rangeTarget = rangeStmt.getX();
                                            
                                            Ident lenIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "len", null);
                                            CallExpr lenCall = new CallExpr(Tree.randomId(), Space.build(" "), Markers.EMPTY, lenIdent, Collections.singletonList(rangeTarget.withPrefix(Space.EMPTY)), false, null);
                                            
                                            List<Expr> newArgs = new ArrayList<>(makeCall.getArgs());
                                            newArgs.add(lenCall);
                                            
                                            CallExpr newMakeCall = makeCall.withArgs(newArgs);
                                            AssignStmt newAssign = assign.withRhs(Collections.singletonList(newMakeCall));
                                            
                                            newStmts.set(i, newAssign);
                                            changed = true;
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

                return b;
            }
            
            private boolean assignsToMap(RangeStmt rangeStmt, String mapName) {
                if (rangeStmt.getBody() == null || rangeStmt.getBody().getStmts() == null) {
                    return false;
                }
                for (Stmt stmt : rangeStmt.getBody().getStmts()) {
                    if (stmt instanceof AssignStmt) {
                        AssignStmt assign = (AssignStmt) stmt;
                        if (assign.getLhs() != null && assign.getLhs().size() == 1 &&
                            assign.getLhs().get(0) instanceof IndexExpr) {
                            IndexExpr idx = (IndexExpr) assign.getLhs().get(0);
                            if (idx.getX() instanceof Ident && mapName.equals(((Ident) idx.getX()).getName())) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        };
    }
}
