package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class ReceiverNaming extends Recipe {

    @Override
    public String getDisplayName() {
        return "Consistent receiver naming";
    }

    @Override
    public String getDescription() {
        return "Renames generic receiver names like 'this', 'self', or 'me' to a single letter based on the type name.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public FuncDecl visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                FuncDecl f = (FuncDecl) super.visitFuncDecl(funcDecl, ctx);

                if (f.getRecv() != null) {
                    Field recvField = f.getRecv();
                    if (recvField.getNames() != null && !recvField.getNames().isEmpty()) {
                        String oldName = recvField.getNames().get(0);
                        
                        if ("this".equals(oldName) || "self".equals(oldName) || "me".equals(oldName)) {
                            String typeName = getTypeName(recvField.getType());
                            if (typeName != null && !typeName.isEmpty()) {
                                String newName = typeName.substring(0, 1).toLowerCase();
                                
                                // Rename in the receiver
                                Field newRecvField = recvField.withNames(java.util.Collections.singletonList(newName));
                                f = f.withRecv(newRecvField);
                                
                                // Rename in the body
                                if (f.getBody() != null) {
                                    f = f.withBody((BlockStmt) new RenameVisitor(oldName, newName).visit(f.getBody(), ctx));
                                }
                            }
                        }
                    }
                }
                
                return f;
            }

            private String getTypeName(Expr type) {
                if (type instanceof Ident) {
                    return ((Ident) type).getName();
                } else if (type instanceof StarExpr) {
                    return getTypeName(((StarExpr) type).getX());
                }
                return null;
            }
        };
    }
    
    private static class RenameVisitor extends GoVisitor<ExecutionContext> {
        private final String oldName;
        private final String newName;
        
        public RenameVisitor(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }
        
        @Override
        public Ident visitIdent(Ident ident, ExecutionContext ctx) {
            if (oldName.equals(ident.getName())) {
                return ident.withName(newName);
            }
            return ident;
        }
    }
}
