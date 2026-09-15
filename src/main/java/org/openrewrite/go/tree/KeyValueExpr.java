package org.openrewrite.go.tree;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.openrewrite.marker.Markers;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@With
public class KeyValueExpr implements Expr {
    @EqualsAndHashCode.Include
    UUID id;
    Space prefix;
    Markers markers;
    
    Expr key;
    Expr value;

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitKeyValueExpr(this, p);
    }
}
