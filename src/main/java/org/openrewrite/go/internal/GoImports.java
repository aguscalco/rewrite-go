package org.openrewrite.go.internal;

import org.openrewrite.Tree;
import org.openrewrite.go.tree.BasicLit;
import org.openrewrite.go.tree.GoFile;
import org.openrewrite.go.tree.ImportDecl;
import org.openrewrite.go.tree.ImportSpec;
import org.openrewrite.go.tree.Space;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.List;

/**
 * Add and remove Go imports on a {@link GoFile}.
 * <p>
 * Recipes that synthesize a qualified call (for example {@code errors.Is}) must add the
 * corresponding import, or the output is not compilable Go. Apply these in an overridden
 * {@code visitGoFile} after the rest of the tree has been visited.
 */
public final class GoImports {

    private static final Space SPEC_PREFIX = Space.build("\n\t");

    private GoImports() {
    }

    /**
     * Strips the surrounding quotes from an import path literal. {@code "\"io\"" -> "io"}.
     */
    public static String unquote(String pathLiteral) {
        if (pathLiteral == null) {
            return "";
        }
        return pathLiteral.replace("\"", "");
    }

    public static boolean hasImport(GoFile file, String path) {
        for (ImportDecl decl : file.getImports()) {
            for (ImportSpec spec : decl.getSpecs()) {
                if (path.equals(unquote(spec.getPath().getValue()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds {@code path} to the file's imports, or returns the file unchanged if already present.
     */
    public static GoFile addImport(GoFile file, String path) {
        if (hasImport(file, path)) {
            return file;
        }

        ImportSpec spec = new ImportSpec(
            Tree.randomId(),
            SPEC_PREFIX,
            Markers.EMPTY,
            null,
            new BasicLit(Tree.randomId(), Space.EMPTY, Markers.EMPTY, "STRING", "\"" + path + "\"")
        );

        List<ImportDecl> decls = new ArrayList<>(file.getImports());
        if (decls.isEmpty()) {
            decls.add(new ImportDecl(
                Tree.randomId(),
                Space.build("\n"),
                Markers.EMPTY,
                new ArrayList<>(java.util.Collections.singletonList(spec)),
                false,
                Space.EMPTY
            ));
            return file.withImports(decls);
        }

        int last = decls.size() - 1;
        ImportDecl target = decls.get(last);
        List<ImportSpec> specs = new ArrayList<>(target.getSpecs());
        specs.add(spec);
        decls.set(last, regroup(target, specs));
        return file.withImports(decls);
    }

    /**
     * Removes {@code path} from the file's imports. An import declaration left with no specs
     * is dropped entirely.
     */
    public static GoFile removeImport(GoFile file, String path) {
        List<ImportDecl> decls = new ArrayList<>();
        boolean changed = false;

        for (ImportDecl decl : file.getImports()) {
            List<ImportSpec> kept = new ArrayList<>();
            for (ImportSpec spec : decl.getSpecs()) {
                if (path.equals(unquote(spec.getPath().getValue()))) {
                    changed = true;
                } else {
                    kept.add(spec);
                }
            }
            if (!kept.isEmpty()) {
                decls.add(kept.size() == decl.getSpecs().size() ? decl : regroup(decl, kept));
            } else if (decl.getSpecs().isEmpty()) {
                decls.add(decl);
            }
        }

        return changed ? file.withImports(decls) : file;
    }

    /**
     * Keeps {@code grouped} consistent with the spec count. {@code GoPrinter} only prints the
     * first spec of an ungrouped declaration, so a multi-spec declaration must be grouped or
     * the remaining imports are silently dropped on print.
     */
    private static ImportDecl regroup(ImportDecl decl, List<ImportSpec> specs) {
        return decl.withSpecs(specs).withGrouped(specs.size() > 1);
    }
}
