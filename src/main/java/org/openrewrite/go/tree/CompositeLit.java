package org.openrewrite.go.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.marker.Markers;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

import java.util.List;
import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@With
public class CompositeLit implements Expr {
    @EqualsAndHashCode.Include
    UUID id;
    Space prefix;
    Markers markers;
    
    @Nullable
    Expr type;
    
    List<Expr> elts;
    
    @Nullable
    GoType resolvedType;

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitCompositeLit(this, p);
    }
}
