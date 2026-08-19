package org.openrewrite.go.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;

import java.util.List;
import java.util.UUID;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public final class ValueSpec implements Go, Spec {
    
    @With
    @EqualsAndHashCode.Include
    @Getter
    UUID id;
    
    @With
    @Getter
    Space prefix;
    
    @With
    @Getter
    Markers markers;
    
    @With
    @Getter
    List<Ident> names;
    
    @With
    @Getter
    Expr type;
    
    @With
    @Getter
    List<Expr> values;
    
    @Override
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitValueSpec(this, p);
    }
}
