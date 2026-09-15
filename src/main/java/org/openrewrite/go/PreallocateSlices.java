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
 * Converts unallocated slices built with append() loops into preallocated slices.
 */
public class PreallocateSlices extends Recipe {

    @Override
    public String getDisplayName() {
        return "Preallocate slices";
    }

    @Override
    public String getDescription() {
        return "Converts dynamically appending loops into preallocated slices when the capacity is determinable from a range loop.";
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

                    // Look for `var s []T`
                    if (s1 instanceof DeclStmt) {
                        DeclStmt declStmt = (DeclStmt) s1;
                        if (declStmt.getDecl() instanceof GenDecl) {
                            GenDecl genDecl = (GenDecl) declStmt.getDecl();
                            if ("var".equals(genDecl.getTok()) && genDecl.getSpecs() != null && genDecl.getSpecs().size() == 1) {
                                Spec spec = genDecl.getSpecs().get(0);
                                if (spec instanceof ValueSpec) {
                                    ValueSpec valueSpec = (ValueSpec) spec;
                                    if (valueSpec.getNames() != null && valueSpec.getNames().size() == 1 &&
                                        valueSpec.getType() instanceof SliceTypeExpr &&
                                        (valueSpec.getValues() == null || valueSpec.getValues().isEmpty())) {
                                        
                                        String sliceName = valueSpec.getNames().get(0).getName();
                                        SliceTypeExpr sliceType = (SliceTypeExpr) valueSpec.getType();
                                        
                                        // Look for `for _, x := range items { s = append(s, ...) }`
                                        if (s2 instanceof RangeStmt) {
                                            RangeStmt rangeStmt = (RangeStmt) s2;
                                            if (appendsToSlice(rangeStmt, sliceName)) {
                                                // Create `s := make([]T, 0, len(items))`
                                                Ident makeIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "make", null);
                                                
                                                Expr rangeTarget = rangeStmt.getX();
                                                
                                                Ident lenIdent = new Ident(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "len", null);
                                                CallExpr lenCall = new CallExpr(Tree.randomId(), Space.build(" "), Markers.EMPTY, lenIdent, Collections.singletonList(rangeTarget.withPrefix(Space.EMPTY)), false, null);
                                                
                                                BasicLit zero = new BasicLit(Tree.randomId(), Space.build(" "), Markers.EMPTY, "INT", "0");
                                                
                                                CallExpr makeCall = new CallExpr(Tree.randomId(), Space.build(" "), Markers.EMPTY, makeIdent, Arrays.asList(sliceType.withPrefix(Space.EMPTY), zero, lenCall), false, null);
                                                
                                                AssignStmt makeAssign = new AssignStmt(Tree.randomId(), declStmt.getPrefix(), Markers.EMPTY,
                                                        Collections.singletonList(valueSpec.getNames().get(0).withPrefix(Space.EMPTY)),
                                                        ":=",
                                                        Collections.singletonList(makeCall));
                                                
                                                newStmts.set(i, makeAssign);
                                                changed = true;
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
            
            private boolean appendsToSlice(RangeStmt rangeStmt, String sliceName) {
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
                                    if (call.getArgs() != null && !call.getArgs().isEmpty() &&
                                        call.getArgs().get(0) instanceof Ident && sliceName.equals(((Ident) call.getArgs().get(0)).getName())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        };
    }
}
