package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class HardcodedSecretRemoval extends Recipe {

    @Override
    public String getDisplayName() {
        return "Extract hardcoded secrets to os.Getenv()";
    }

    @Override
    public String getDescription() {
        return "Detects hardcoded secrets (tokens, passwords, api keys) in string literals and extracts them to `os.Getenv(\"SECRET\")`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public AssignStmt visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = (AssignStmt) super.visitAssignStmt(assignStmt, ctx);

                if (a.getLhs() != null && a.getLhs().size() == 1 && a.getRhs() != null && a.getRhs().size() == 1) {
                    Expr lhs = a.getLhs().get(0);
                    Expr rhs = a.getRhs().get(0);

                    if (lhs instanceof Ident && rhs instanceof BasicLit) {
                        String name = ((Ident) lhs).getName().toLowerCase();
                        BasicLit lit = (BasicLit) rhs;
                        
                        if ("STRING".equals(lit.getKind()) && lit.getValue() != null && lit.getValue().length() > 2) {
                            if (name.contains("secret") || name.contains("password") || name.contains("token") || name.contains("apikey")) {
                                
                                // Create os.Getenv("SECRET_VAR")
                                Ident osIdent = new Ident(UUID.randomUUID(), rhs.getPrefix(), Markers.EMPTY, "os", null);
                                Ident getenvIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Getenv", null);
                                SelectorExpr osGetenv = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osIdent, getenvIdent, null);
                                
                                String envVarName = ((Ident) lhs).getName().toUpperCase();
                                BasicLit envArg = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"" + envVarName + "\"");
                                
                                CallExpr call = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, osGetenv, Collections.singletonList(envArg), false, null);
                                
                                return a.withRhs(Collections.singletonList(call));
                            }
                        }
                    }
                }

                return a;
            }
        };
    }
}
