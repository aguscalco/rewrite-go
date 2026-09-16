package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class BytesHasPrefixEq extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.HasPrefix over bytes.Index == 0";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.Index(s, \"prefix\") == 0` to `bytes.HasPrefix(s, \"prefix\")`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("==".equals(b.getOp())) {
                    if (b.getX() instanceof CallExpr && b.getY() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getX();
                        BasicLit lit = (BasicLit) b.getY();
                        
                        if ("0".equals(lit.getValue()) && isBytesIndex(call)) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            Ident hasPrefixIdent = sel.getSel().withName("HasPrefix");
                            return call.withFun(sel.withSel(hasPrefixIdent)).withPrefix(b.getPrefix());
                        }
                    } else if (b.getY() instanceof CallExpr && b.getX() instanceof BasicLit) {
                        CallExpr call = (CallExpr) b.getY();
                        BasicLit lit = (BasicLit) b.getX();
                        
                        if ("0".equals(lit.getValue()) && isBytesIndex(call)) {
                            SelectorExpr sel = (SelectorExpr) call.getFun();
                            Ident hasPrefixIdent = sel.getSel().withName("HasPrefix");
                            return call.withFun(sel.withSel(hasPrefixIdent)).withPrefix(b.getPrefix());
                        }
                    }
                }

                return b;
            }

            private boolean isBytesIndex(CallExpr call) {
                if (call.getFun() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) call.getFun();
                    if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName())) {
                        return "Index".equals(sel.getSel().getName());
                    }
                }
                return false;
            }
        };
    }
}
