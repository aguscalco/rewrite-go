package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

public class GoVisitor<P> extends TreeVisitor<Tree, P> {
    
    @Override
    public boolean isAcceptable(Tree sourceFile, P p) {
        return false;
    }
    
    @Override
    public String getLanguage() {
        return "go";
    }
}
