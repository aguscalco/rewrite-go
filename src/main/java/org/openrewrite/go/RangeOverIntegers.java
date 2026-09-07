package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class RangeOverIntegers extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use range over integers";
    }
    
    @Override
    public String getDescription() {
        return "Convert for i := 0; i < n; i++ to for i := range n (Go 1.22+).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitForStmt(ForStmt forStmt, ExecutionContext ctx) {
                ForStmt f = (ForStmt) super.visitForStmt(forStmt, ctx);
                
                if (!isSimpleCountingLoop(f)) {
                    return f;
                }
                
                AssignStmt init = (AssignStmt) f.getInit();
                BinaryExpr cond = (BinaryExpr) f.getCond();
                
                Ident loopVar = (Ident) init.getLhs().get(0);
                Expr limit = cond.getY();
                
                return new RangeStmt(
                    Tree.randomId(),
                    f.getPrefix(),
                    f.getMarkers(),
                    loopVar,
                    null,
                    limit,
                    ":=",
                    f.getBody()
                );
            }
            
            private boolean isSimpleCountingLoop(ForStmt f) {
                if (f.getInit() == null || f.getCond() == null || f.getPost() == null) {
                    return false;
                }
                
                if (!(f.getInit() instanceof AssignStmt)) {
                    return false;
                }
                
                AssignStmt init = (AssignStmt) f.getInit();
                if (!":=".equals(init.getTok()) || init.getLhs().size() != 1 || init.getRhs().size() != 1) {
                    return false;
                }
                
                if (!(init.getLhs().get(0) instanceof Ident)) {
                    return false;
                }
                
                if (!(init.getRhs().get(0) instanceof BasicLit)) {
                    return false;
                }
                
                BasicLit initVal = (BasicLit) init.getRhs().get(0);
                if (!"0".equals(initVal.getValue())) {
                    return false;
                }
                
                if (!(f.getCond() instanceof BinaryExpr)) {
                    return false;
                }
                
                BinaryExpr cond = (BinaryExpr) f.getCond();
                if (!"<".equals(cond.getOp())) {
                    return false;
                }
                
                if (!(cond.getX() instanceof Ident)) {
                    return false;
                }
                
                Ident condVar = (Ident) cond.getX();
                Ident initVar = (Ident) init.getLhs().get(0);
                if (!condVar.getName().equals(initVar.getName())) {
                    return false;
                }
                
                if (!(f.getPost() instanceof IncDecStmt)) {
                    return false;
                }
                
                IncDecStmt post = (IncDecStmt) f.getPost();
                if (!"++".equals(post.getTok())) {
                    return false;
                }
                
                if (!(post.getX() instanceof Ident)) {
                    return false;
                }
                
                Ident postVar = (Ident) post.getX();
                return postVar.getName().equals(initVar.getName());
            }
        };
    }
}
