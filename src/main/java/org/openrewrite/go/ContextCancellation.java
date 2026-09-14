package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Ensures proper context cancellation by adding defer cancel() statements.
 *
 * This recipe identifies context creation with cancellation (context.WithCancel,
 * context.WithTimeout, context.WithDeadline) and ensures the cancel function
 * is called via defer to prevent resource leaks.
 *
 * Example:
 *   ctx, cancel := context.WithTimeout(context.Background(), timeout)
 *   // Missing defer cancel()
 *
 *   becomes:
 *   ctx, cancel := context.WithTimeout(context.Background(), timeout)
 *   defer cancel()
 */
public class ContextCancellation extends Recipe {

    @Option(displayName = "Context variable name",
            description = "The context variable name to look for",
            example = "ctx")
    String contextVarName = "ctx";

    @Override
    public String getDisplayName() {
        return "Ensure context cancellation";
    }

    @Override
    public String getDescription() {
        return "Add defer cancel() after context creation with cancellation functions.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);
                
                boolean changed = false;
                List<Stmt> newStmts = new ArrayList<>();
                
                for (int i = 0; i < b.getStmts().size(); i++) {
                    Stmt stmt = b.getStmts().get(i);
                    newStmts.add(stmt);
                    
                    if (isContextWithCancel(stmt) && !hasDeferCancel(b.getStmts(), i)) {
                        Ident cancelIdent = new Ident(
                            Tree.randomId(),
                            Space.EMPTY,
                            stmt.getMarkers(),
                            "cancel",
                            null
                        );
                        
                        CallExpr cancelCall = new CallExpr(
                            Tree.randomId(),
                            Space.EMPTY,
                            stmt.getMarkers(),
                            cancelIdent,
                            java.util.Collections.emptyList(),
                            false,
                            null
                        );
                        
                        DeferStmt deferStmt = new DeferStmt(
                            Tree.randomId(),
                            Space.build("\n"),
                            stmt.getMarkers(),
                            cancelCall
                        );
                        
                        newStmts.add(deferStmt);
                        changed = true;
                    }
                }
                
                return changed ? b.withStmts(newStmts) : b;
            }
            
            private boolean isContextWithCancel(Stmt stmt) {
                if (!(stmt instanceof AssignStmt)) {
                    return false;
                }
                
                AssignStmt assign = (AssignStmt) stmt;
                
                if (assign.getLhs().size() != 2 || assign.getRhs().size() != 1) {
                    return false;
                }
                
                if (!(assign.getLhs().get(0) instanceof Ident)) {
                    return false;
                }
                
                Ident lhsCtx = (Ident) assign.getLhs().get(0);
                if (!contextVarName.equals(lhsCtx.getName())) {
                    return false;
                }
                
                if (!(assign.getLhs().get(1) instanceof Ident)) {
                    return false;
                }
                
                Ident lhsCancel = (Ident) assign.getLhs().get(1);
                if (!"cancel".equals(lhsCancel.getName())) {
                    return false;
                }
                
                if (!(assign.getRhs().get(0) instanceof CallExpr)) {
                    return false;
                }
                
                CallExpr call = (CallExpr) assign.getRhs().get(0);
                if (!(call.getFun() instanceof SelectorExpr)) {
                    return false;
                }
                
                SelectorExpr sel = (SelectorExpr) call.getFun();
                if (!(sel.getX() instanceof Ident)) {
                    return false;
                }
                
                Ident pkg = (Ident) sel.getX();
                Ident method = sel.getSel();
                
                if (!"context".equals(pkg.getName())) {
                    return false;
                }
                
                String methodName = method.getName();
                return "WithCancel".equals(methodName) || 
                       "WithTimeout".equals(methodName) || 
                       "WithDeadline".equals(methodName);
            }
            
            private boolean hasDeferCancel(List<Stmt> stmts, int afterIndex) {
                for (int i = afterIndex + 1; i < stmts.size(); i++) {
                    Stmt stmt = stmts.get(i);
                    if (stmt instanceof DeferStmt) {
                        DeferStmt defer = (DeferStmt) stmt;
                        if (defer.getCall() instanceof CallExpr) {
                            CallExpr call = (CallExpr) defer.getCall();
                            if (call.getFun() instanceof Ident) {
                                Ident funIdent = (Ident) call.getFun();
                                if ("cancel".equals(funIdent.getName()) && call.getArgs().isEmpty()) {
                                    return true;
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
