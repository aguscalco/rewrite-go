package org.openrewrite.go.tree;

import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface Go extends Tree {
    
    @Override
    default <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return v.adapt(GoVisitor.class).visitGo(this, p);
    }
    
    @Override
    default <P> boolean isAcceptable(TreeVisitor<?, P> v, P p) {
        return v.isAdaptableTo(GoVisitor.class);
    }
    
    Space getPrefix();
    
    <G extends Go> G withPrefix(Space prefix);
    
    Markers getMarkers();
    
    <G extends Go> G withMarkers(Markers markers);
}
