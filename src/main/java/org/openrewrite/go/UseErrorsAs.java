package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

import java.util.Arrays;

public class UseErrorsAs extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "Use errors.As";
    }
    
    @Override
    public String getDescription() {
        return "Replace type assertions on errors with errors.As(err, &target).";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            public Tree visitAssignStmt(AssignStmt assignStmt, ExecutionContext ctx) {
                AssignStmt a = assignStmt;
                
                // Check if this is a type assertion assignment: target, ok := err.(*MyError)
                if (":=".equals(a.getTok()) && a.getLhs().size() == 2 && a.getRhs().size() == 1) {
                    Expr rhs = a.getRhs().get(0);
                    
                    // Check if RHS is a type assertion
                    if (rhs instanceof TypeAssertExpr) {
                        TypeAssertExpr typeAssert = (TypeAssertExpr) rhs;
                        
                        // Check if the expression being asserted is an error variable
                        if (typeAssert.getX() instanceof Ident) {
                            Ident errIdent = (Ident) typeAssert.getX();
                            if ("err".equals(errIdent.getName()) || "e".equals(errIdent.getName())) {
                                // Convert to errors.As
                                return convertToErrorsAs(a, typeAssert);
                            }
                        }
                    }
                }
                
                return a;
            }
            
            private AssignStmt convertToErrorsAs(AssignStmt assignStmt, TypeAssertExpr typeAssert) {
                // Create errors.As(err, &target)
                Ident errorsIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    assignStmt.getMarkers(),
                    "errors",
                    null
                );
                
                Ident asIdent = new Ident(
                    Tree.randomId(),
                    Space.EMPTY,
                    assignStmt.getMarkers(),
                    "As",
                    null
                );
                
                SelectorExpr errorsAsSelector = new SelectorExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    assignStmt.getMarkers(),
                    errorsIdent,
                    asIdent,
                    null
                );
                
                // Get the target variable (first LHS)
                Expr targetVar = assignStmt.getLhs().get(0);
                
                // Create &target
                UnaryExpr addrTarget = new UnaryExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    assignStmt.getMarkers(),
                    "&",
                    targetVar,
                    null
                );
                
                // Create errors.As(err, &target)
                CallExpr errorsAsCall = new CallExpr(
                    Tree.randomId(),
                    Space.EMPTY,
                    assignStmt.getMarkers(),
                    errorsAsSelector,
                    Arrays.asList(typeAssert.getX(), addrTarget),
                    false,
                    null
                );
                
                // Replace RHS with errors.As call
                return assignStmt.withRhs(java.util.Collections.singletonList(errorsAsCall));
            }
        };
    }
}
