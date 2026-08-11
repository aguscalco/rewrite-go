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
public final class GoType implements Go {
    
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
    Type type;
    
    public interface Type {
    }
    
    @Data
    public static final class Basic implements Type {
        String kind;
    }
    
    @Data
    public static final class Named implements Type {
        String packagePath;
        String name;
        GoType underlying;
    }
    
    @Data
    public static final class Pointer implements Type {
        GoType elem;
    }
    
    @Data
    public static final class Array implements Type {
        long len;
        GoType elem;
    }
    
    @Data
    public static final class Slice implements Type {
        GoType elem;
    }
    
    @Data
    public static final class Map implements Type {
        GoType key;
        GoType value;
    }
    
    @Data
    public static final class Chan implements Type {
        Direction dir;
        GoType elem;
        
        public enum Direction {
            SEND_RECV,
            SEND_ONLY,
            RECV_ONLY
        }
    }
    
    @Data
    public static final class Func implements Type {
        java.util.List<Field> params;
        java.util.List<Field> results;
        java.util.List<TypeParamDecl> typeParams;
    }
    
    @Data
    public static final class Interface implements Type {
        java.util.List<Method> methods;
    }
    
    @Data
    public static final class Struct implements Type {
        java.util.List<Field> fields;
    }
    
    @Data
    public static final class TypeParameter implements Type {
        String name;
    }
    
    @Override
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitGoType(this, p);
    }
}
