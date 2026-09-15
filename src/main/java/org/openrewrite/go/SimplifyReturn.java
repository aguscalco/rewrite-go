package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Collections;

public class SimplifyReturn extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify return statement";
    }

    @Override
    public String getDescription() {
        return "Simplifies if-else blocks that just return boolean literals into a single return statement.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public IfStmt visitIfStmt(IfStmt ifStmt, ExecutionContext ctx) {
                IfStmt i = (IfStmt) super.visitIfStmt(ifStmt, ctx);

                if (i.getInit() == null && i.getElseStmt() instanceof BlockStmt) {
                    BlockStmt ifBlock = i.getBody();
                    BlockStmt elseBlock = (BlockStmt) i.getElseStmt();
                    
                    if (ifBlock.getStmts() != null && ifBlock.getStmts().size() == 1 &&
                        elseBlock.getStmts() != null && elseBlock.getStmts().size() == 1) {
                        
                        Stmt ifS = ifBlock.getStmts().get(0);
                        Stmt elseS = elseBlock.getStmts().get(0);
                        
                        if (ifS instanceof ReturnStmt && elseS instanceof ReturnStmt) {
                            ReturnStmt ifRet = (ReturnStmt) ifS;
                            ReturnStmt elseRet = (ReturnStmt) elseS;
                            
                            if (ifRet.getResults() != null && ifRet.getResults().size() == 1 &&
                                elseRet.getResults() != null && elseRet.getResults().size() == 1) {
                                
                                Expr ifExpr = ifRet.getResults().get(0);
                                Expr elseExpr = elseRet.getResults().get(0);
                                
                                if (ifExpr instanceof Ident && elseExpr instanceof Ident) {
                                    String ifVal = ((Ident) ifExpr).getName();
                                    String elseVal = ((Ident) elseExpr).getName();
                                    
                                    if ("true".equals(ifVal) && "false".equals(elseVal)) {
                                        // return cond
                                        ReturnStmt newRet = new ReturnStmt(java.util.UUID.randomUUID(), i.getPrefix(), Markers.EMPTY, 
                                                Collections.singletonList(i.getCond().withPrefix(Space.build(" "))));
                                        // But we can't easily replace the IfStmt with a ReturnStmt in this visitor
                                        // We have to do it in visitBlockStmt
                                    }
                                }
                            }
                        }
                    }
                }

                return i;
            }
            
            @Override
            public BlockStmt visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);
                if (b.getStmts() == null || b.getStmts().isEmpty()) return b;
                
                boolean changed = false;
                java.util.List<Stmt> newStmts = new java.util.ArrayList<>(b.getStmts());
                
                for (int idx = 0; idx < newStmts.size(); idx++) {
                    Stmt s = newStmts.get(idx);
                    if (s instanceof IfStmt) {
                        IfStmt i = (IfStmt) s;
                        if (i.getInit() == null && i.getElseStmt() instanceof BlockStmt) {
                            BlockStmt ifBlock = i.getBody();
                            BlockStmt elseBlock = (BlockStmt) i.getElseStmt();
                            
                            if (ifBlock.getStmts() != null && ifBlock.getStmts().size() == 1 &&
                                elseBlock.getStmts() != null && elseBlock.getStmts().size() == 1) {
                                
                                Stmt ifS = ifBlock.getStmts().get(0);
                                Stmt elseS = elseBlock.getStmts().get(0);
                                
                                if (ifS instanceof ReturnStmt && elseS instanceof ReturnStmt) {
                                    ReturnStmt ifRet = (ReturnStmt) ifS;
                                    ReturnStmt elseRet = (ReturnStmt) elseS;
                                    
                                    if (ifRet.getResults() != null && ifRet.getResults().size() == 1 &&
                                        elseRet.getResults() != null && elseRet.getResults().size() == 1) {
                                        
                                        Expr ifExpr = ifRet.getResults().get(0);
                                        Expr elseExpr = elseRet.getResults().get(0);
                                        
                                        if (ifExpr instanceof Ident && elseExpr instanceof Ident) {
                                            String ifVal = ((Ident) ifExpr).getName();
                                            String elseVal = ((Ident) elseExpr).getName();
                                            
                                            if ("true".equals(ifVal) && "false".equals(elseVal)) {
                                                ReturnStmt newRet = new ReturnStmt(java.util.UUID.randomUUID(), i.getPrefix(), Markers.EMPTY, 
                                                        Collections.singletonList(i.getCond().withPrefix(Space.build(" "))));
                                                newStmts.set(idx, newRet);
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
        };
    }
}
