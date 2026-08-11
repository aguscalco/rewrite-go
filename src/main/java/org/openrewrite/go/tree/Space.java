package org.openrewrite.go.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;

import java.util.UUID;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
public final class Space {
    
    public static final Space EMPTY = new Space("");
    
    @With
    @EqualsAndHashCode.Include
    @Getter
    String whitespace;
    
    public static Space build(String whitespace) {
        if (whitespace == null || whitespace.isEmpty()) {
            return EMPTY;
        }
        return new Space(whitespace);
    }
    
    public boolean isEmpty() {
        return whitespace.isEmpty();
    }
}
