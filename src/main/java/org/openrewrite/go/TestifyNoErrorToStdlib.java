package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestifyNoErrorToStdlib extends Recipe {

    @Override
    public String getDisplayName() {
        return "Migrate testify assert.NoError to standard library";
    }

    @Override
    public String getDescription() {
        return "Converts `assert.NoError(t, err)` to `if err != nil { t.Errorf(...) }` without relying on the testify dependency.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public BlockStmt visitBlockStmt(BlockStmt blockStmt, ExecutionContext ctx) {
                BlockStmt b = (BlockStmt) super.visitBlockStmt(blockStmt, ctx);

                if (b.getStmts() != null) {
                    boolean changed = false;
                    List<Stmt> newStmts = new ArrayList<>(b.getStmts().size());

                    for (Stmt stmt : b.getStmts()) {
                        if (stmt instanceof ExprStmt) {
                            ExprStmt exprStmt = (ExprStmt) stmt;
                            if (exprStmt.getExpr() instanceof CallExpr) {
                                CallExpr call = (CallExpr) exprStmt.getExpr();
                                if (call.getFun() instanceof SelectorExpr) {
                                    SelectorExpr sel = (SelectorExpr) call.getFun();
                                    if (sel.getX() instanceof Ident && "assert".equals(((Ident) sel.getX()).getName())) {
                                        if (("NoError".equals(sel.getSel().getName()) || "Nil".equals(sel.getSel().getName())) && call.getArgs() != null && call.getArgs().size() == 2) {
                                            
                                            // Construct: if err != nil { t.Errorf("expected no error, got %v", err) }
                                            Expr tArg = call.getArgs().get(0);
                                            Expr errArg = call.getArgs().get(1);
                                            
                                            // err != nil
                                            Ident nilIdent = new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "nil", null);
                                            BinaryExpr condition = new BinaryExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, errArg, "!=", nilIdent, null);
                                            
                                            // t.Errorf(...)
                                            Ident errorfIdent = new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Errorf", null);
                                            SelectorExpr tErrorf = new SelectorExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tArg.withPrefix(Space.build("\n\t\t")), errorfIdent, null);
                                            BasicLit msg = new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"expected no error, got %v\"");
                                            CallExpr errorfCall = new CallExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, tErrorf, Arrays.asList(msg, errArg.withPrefix(Space.build(" "))), false, null);
                                            ExprStmt errorfStmt = new ExprStmt(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, errorfCall);
                                            
                                            BlockStmt ifBody = new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.singletonList(errorfStmt), Space.build("\n\t"));
                                            
                                            IfStmt ifStmt = new IfStmt(UUID.randomUUID(), stmt.getPrefix(), Markers.EMPTY, null, condition.withPrefix(Space.build(" ")), ifBody, null);
                                            
                                            newStmts.add(ifStmt);
                                            changed = true;
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                        newStmts.add(stmt);
                    }

                    if (changed) {
                        return b.withStmts(newStmts);
                    }
                }

                return b;
            }
        };
    }
}
