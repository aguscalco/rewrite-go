package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OrganizeImports extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Organize Go imports";
    }
    
    @Override
    public String getDescription() {
        return "Sort and group Go imports into standard library and third-party packages.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitGoFile(GoFile goFile, ExecutionContext ctx) {
                if (goFile.getImports().isEmpty()) {
                    return goFile;
                }
                
                List<ImportSpec> allSpecs = new ArrayList<>();
                for (ImportDecl imp : goFile.getImports()) {
                    allSpecs.addAll(imp.getSpecs());
                }
                
                List<ImportSpec> stdlib = new ArrayList<>();
                List<ImportSpec> thirdParty = new ArrayList<>();
                
                for (ImportSpec spec : allSpecs) {
                    String path = spec.getPath().getValue();
                    if (isStdlib(path)) {
                        stdlib.add(spec);
                    } else {
                        thirdParty.add(spec);
                    }
                }
                
                stdlib.sort(Comparator.comparing(s -> s.getPath().getValue()));
                thirdParty.sort(Comparator.comparing(s -> s.getPath().getValue()));
                
                List<ImportDecl> newImports = new ArrayList<>();
                
                if (!stdlib.isEmpty()) {
                    newImports.add(new ImportDecl(
                        Tree.randomId(),
                        Space.build("\n"),
                        goFile.getImports().get(0).getMarkers(),
                        stdlib,
                        false,
                        Space.EMPTY
                    ));
                }
                
                if (!thirdParty.isEmpty()) {
                    newImports.add(new ImportDecl(
                        Tree.randomId(),
                        Space.build("\n"),
                        goFile.getImports().get(0).getMarkers(),
                        thirdParty,
                        false,
                        Space.EMPTY
                    ));
                }
                
                return goFile.withImports(newImports);
            }
        };
    }
    
    private boolean isStdlib(String importPath) {
        String path = importPath.replaceAll("\"", "");
        
        if (path.contains(".")) {
            return false;
        }
        
        String[] stdlibPrefixes = {
            "archive/", "bufio", "builtin", "bytes", "compress/", "container/",
            "context", "crypto/", "database/", "debug/", "encoding/", "errors",
            "expvar", "flag", "fmt", "go/", "hash/", "html/", "image/", "index/",
            "io", "log", "math", "mime", "net", "os", "path", "plugin", "reflect",
            "regexp", "runtime", "sort", "strconv", "strings", "sync", "syscall",
            "testing", "text/", "time", "unicode", "unsafe"
        };
        
        for (String prefix : stdlibPrefixes) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        
        return false;
    }
}
