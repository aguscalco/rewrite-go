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
public final class GenDecl implements Go, Decl {
    
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
    String tok;
    
    @With
    @Getter
    List<Spec> specs;
    
    @With
    @Getter
    boolean grouped;
    
    @With
    @Getter
    Space end;
    
    @Override
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitGenDecl(this, p);
    }
}
