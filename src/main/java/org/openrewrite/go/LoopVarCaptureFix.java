package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

public class LoopVarCaptureFix extends Recipe {

    @Override
    public String getDisplayName() {
        return "Remove redundant loop variable captures";
    }

    @Override
    public String getDescription() {
        return "Removes redundant loop variable captures (e.g. `v := v`) which are no longer necessary in Go 1.22+.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public BlockStmt visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);

                if (b.getStmts() != null) {
                    List<Stmt> newStmts = new ArrayList<>();
                    boolean changed = false;

                    for (Stmt stmt : b.getStmts()) {
                        if (stmt instanceof AssignStmt) {
                            AssignStmt assign = (AssignStmt) stmt;
                            if (":=".equals(assign.getTok()) && assign.getLhs() != null && assign.getRhs() != null && 
                                assign.getLhs().size() == 1 && assign.getRhs().size() == 1) {
                                
                                Expr lhs = assign.getLhs().get(0);
                                Expr rhs = assign.getRhs().get(0);

                                if (lhs instanceof Ident && rhs instanceof Ident) {
                                    String lhsName = ((Ident) lhs).getName();
                                    String rhsName = ((Ident) rhs).getName();
                                    
                                    if (lhsName != null && lhsName.equals(rhsName)) {
                                        changed = true;
                                        continue; // skip adding to newStmts, effectively deleting it
                                    }
                                }
                            }
                        }
                        newStmts.add(stmt);
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
