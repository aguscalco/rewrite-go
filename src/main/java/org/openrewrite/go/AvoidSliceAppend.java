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
 * Converts preallocated append loops to direct index assignment.
 */
public class AvoidSliceAppend extends Recipe {

    @Override
    public String getDisplayName() {
        return "Avoid slice append";
    }

    @Override
    public String getDescription() {
        return "Converts preallocated append loops (make([]T, 0, len)) into direct index assignments (make([]T, len) and s[i] = v).";
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

                    // Look for `s := make([]T, 0, len(items))`
                    if (s1 instanceof AssignStmt) {
                        AssignStmt assign = (AssignStmt) s1;
                        if (":=".equals(assign.getTok()) && assign.getLhs() != null && assign.getLhs().size() == 1 &&
                            assign.getRhs() != null && assign.getRhs().size() == 1) {
                            
                            if (assign.getLhs().get(0) instanceof Ident && assign.getRhs().get(0) instanceof CallExpr) {
                                String sliceName = ((Ident) assign.getLhs().get(0)).getName();
                                CallExpr makeCall = (CallExpr) assign.getRhs().get(0);
                                
                                if (makeCall.getFun() instanceof Ident && "make".equals(((Ident) makeCall.getFun()).getName()) &&
                                    makeCall.getArgs() != null && makeCall.getArgs().size() == 3 &&
                                    makeCall.getArgs().get(0) instanceof SliceTypeExpr &&
                                    makeCall.getArgs().get(1) instanceof BasicLit &&
                                    "0".equals(((BasicLit) makeCall.getArgs().get(1)).getValue()) &&
                                    makeCall.getArgs().get(2) instanceof CallExpr) {
                                    
                                    CallExpr lenCall = (CallExpr) makeCall.getArgs().get(2);
                                    if (lenCall.getFun() instanceof Ident && "len".equals(((Ident) lenCall.getFun()).getName())) {
                                        
                                        // Look for `for idx, val := range items { s = append(s, val) }`
                                        if (s2 instanceof RangeStmt) {
                                            RangeStmt rangeStmt = (RangeStmt) s2;
                                            if (rangeStmt.getKey() instanceof Ident && !"_".equals(((Ident) rangeStmt.getKey()).getName()) &&
                                                rangeStmt.getValue() != null && rangeStmt.getValue() instanceof Ident) {
                                                
                                                Ident idxIdent = (Ident) rangeStmt.getKey();
                                                Ident valIdent = (Ident) rangeStmt.getValue();
                                                
                                                if (appendsToSlice(rangeStmt, sliceName, valIdent.getName())) {
                                                    // Convert to `s := make([]T, len(items))`
                                                    CallExpr newMakeCall = makeCall.withArgs(Arrays.asList(
                                                            makeCall.getArgs().get(0),
                                                            makeCall.getArgs().get(2).withPrefix(Space.build(" "))
                                                    ));
                                                    AssignStmt newAssign = assign.withRhs(Collections.singletonList(newMakeCall));
                                                    
                                                    // Convert to `s[idx] = val`
                                                    RangeStmt newRange = convertAppendToIndex(rangeStmt, sliceName, idxIdent, valIdent);
                                                    
                                                    newStmts.set(i, newAssign);
                                                    newStmts.set(i + 1, newRange);
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

                return b;
            }
            
            private boolean appendsToSlice(RangeStmt rangeStmt, String sliceName, String valName) {
                if (rangeStmt.getBody() == null || rangeStmt.getBody().getStmts() == null) {
                    return false;
                }
                for (Stmt stmt : rangeStmt.getBody().getStmts()) {
                    if (stmt instanceof AssignStmt) {
                        AssignStmt assign = (AssignStmt) stmt;
                        if (assign.getLhs() != null && assign.getLhs().size() == 1 &&
                            assign.getLhs().get(0) instanceof Ident && sliceName.equals(((Ident) assign.getLhs().get(0)).getName())) {
                            
                            if (assign.getRhs() != null && assign.getRhs().size() == 1 &&
                                assign.getRhs().get(0) instanceof CallExpr) {
                                CallExpr call = (CallExpr) assign.getRhs().get(0);
                                if (call.getFun() instanceof Ident && "append".equals(((Ident) call.getFun()).getName())) {
                                    if (call.getArgs() != null && call.getArgs().size() == 2 &&
                                        call.getArgs().get(0) instanceof Ident && sliceName.equals(((Ident) call.getArgs().get(0)).getName()) &&
                                        call.getArgs().get(1) instanceof Ident && valName.equals(((Ident) call.getArgs().get(1)).getName())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
            
            private RangeStmt convertAppendToIndex(RangeStmt rangeStmt, String sliceName, Ident idxIdent, Ident valIdent) {
                List<Stmt> stmts = new ArrayList<>();
                for (Stmt stmt : rangeStmt.getBody().getStmts()) {
                    if (stmt instanceof AssignStmt) {
                        AssignStmt assign = (AssignStmt) stmt;
                        if (assign.getLhs() != null && assign.getLhs().size() == 1 &&
                            assign.getLhs().get(0) instanceof Ident && sliceName.equals(((Ident) assign.getLhs().get(0)).getName())) {
                            
                            Ident sliceIdent = new Ident(Tree.randomId(), assign.getLhs().get(0).getPrefix(), Markers.EMPTY, sliceName, null);
                            IndexExpr indexExpr = new IndexExpr(Tree.randomId(), Space.EMPTY, Markers.EMPTY, sliceIdent, idxIdent.withPrefix(Space.EMPTY));
                            
                            AssignStmt indexAssign = new AssignStmt(Tree.randomId(), assign.getPrefix(), Markers.EMPTY,
                                    Collections.singletonList(indexExpr),
                                    "=",
                                    Collections.singletonList(valIdent.withPrefix(Space.build(" "))));
                            
                            stmts.add(indexAssign);
                            continue;
                        }
                    }
                    stmts.add(stmt);
                }
                return rangeStmt.withBody(rangeStmt.getBody().withStmts(stmts));
            }
        };
    }
}
