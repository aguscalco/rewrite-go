package org.openrewrite.go.tree;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.openrewrite.Cursor;
import org.openrewrite.FileAttributes;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
@Data
public final class GoFile implements Go, org.openrewrite.SourceFile {
    
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
    Path sourcePath;
    
    @With
    @Getter
    Charset charset;
    
    @With
    @Getter
    boolean charsetBomMarked;
    
    @With
    @Getter
    PackageClause packageClause;
    
    @With
    @Getter
    List<ImportDecl> imports;
    
    @With
    @Getter
    List<Decl> declarations;
    
    @With
    @Getter
    Space eof;
    
    @With
    @Getter
    FileAttributes fileAttributes;
    
    @With
    @Getter
    org.openrewrite.Checksum checksum;
    
    @Override
    public <P> TreeVisitor<?, PrintOutputCapture<P>> printer(Cursor cursor) {
        return new GoPrinter<>();
    }
    
    @Override
    public <R extends Tree, P> R accept(TreeVisitor<R, P> v, P p) {
        return (R) v.adapt(GoVisitor.class).visitGoFile(this, p);
    }
}
