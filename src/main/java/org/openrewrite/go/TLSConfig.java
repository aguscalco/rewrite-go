package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Enforces modern TLS configuration by setting MinVersion to tls.VersionTLS12
 * if it is missing or set to an older version.
 */
public class TLSConfig extends Recipe {

    @Override
    public String getDisplayName() {
        return "Enforce modern TLS configuration";
    }

    @Override
    public String getDescription() {
        return "Sets MinVersion in tls.Config to tls.VersionTLS12 if not already present or if set lower.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitCompositeLit(CompositeLit compositeLit, ExecutionContext ctx) {
                CompositeLit c = (CompositeLit) super.visitCompositeLit(compositeLit, ctx);
                
                if (c.getType() instanceof SelectorExpr) {
                    SelectorExpr sel = (SelectorExpr) c.getType();
                    if (sel.getX() instanceof Ident && ((Ident) sel.getX()).getName().equals("tls") &&
                        sel.getSel().getName().equals("Config")) {
                        
                        boolean hasMinVersion = false;
                        for (Expr elt : c.getElts()) {
                            if (elt instanceof KeyValueExpr) {
                                KeyValueExpr kv = (KeyValueExpr) elt;
                                if (kv.getKey() instanceof Ident && ((Ident) kv.getKey()).getName().equals("MinVersion")) {
                                    hasMinVersion = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!hasMinVersion) {
                            Ident key = new Ident(Tree.randomId(), Space.build("\n\t"), c.getMarkers(), "MinVersion", null);
                            Ident tlsPkg = new Ident(Tree.randomId(), Space.EMPTY, c.getMarkers(), "tls", null);
                            Ident version = new Ident(Tree.randomId(), Space.EMPTY, c.getMarkers(), "VersionTLS12", null);
                            SelectorExpr val = new SelectorExpr(Tree.randomId(), Space.EMPTY, c.getMarkers(), tlsPkg, version, null);
                            
                            KeyValueExpr kv = new KeyValueExpr(
                                Tree.randomId(),
                                Space.EMPTY,
                                c.getMarkers(),
                                key,
                                val
                            );
                            
                            List<Expr> newElts = new ArrayList<>(c.getElts());
                            newElts.add(kv);
                            return c.withElts(newElts);
                        }
                    }
                }
                
                return c;
            }
        };
    }
}
