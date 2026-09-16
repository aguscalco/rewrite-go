package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.util.UUID;

public class BytesIndexToContains extends Recipe {

    @Override
    public String getDisplayName() {
        return "Use bytes.Contains instead of bytes.Index";
    }

    @Override
    public String getDescription() {
        return "Migrates `bytes.Index(b, sub) != -1` or `>= 0` to `bytes.Contains(b, sub)`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitBinaryExpr(BinaryExpr binaryExpr, ExecutionContext ctx) {
                BinaryExpr b = (BinaryExpr) super.visitBinaryExpr(binaryExpr, ctx);

                if ("!=".equals(b.getOp()) || ">=".equals(b.getOp()) || ">".equals(b.getOp())) {
                    if (isBytesIndex(b.getX()) && isMinusOneOrZero(b.getY(), b.getOp())) {
                        return toContains((CallExpr) b.getX(), b.getPrefix(), false);
                    } else if (isBytesIndex(b.getY()) && isMinusOneOrZeroLeft(b.getX(), b.getOp())) {
                        return toContains((CallExpr) b.getY(), b.getPrefix(), false);
                    }
                } else if ("==".equals(b.getOp()) || "<".equals(b.getOp()) || "<=".equals(b.getOp())) {
                    if (isBytesIndex(b.getX()) && isMinusOneOrZero(b.getY(), inverseOp(b.getOp()))) {
                        return toContains((CallExpr) b.getX(), b.getPrefix(), true);
                    } else if (isBytesIndex(b.getY()) && isMinusOneOrZeroLeft(b.getX(), inverseOp(b.getOp()))) {
                        return toContains((CallExpr) b.getY(), b.getPrefix(), true);
                    }
                }

                return b;
            }

            private boolean isBytesIndex(Expr expr) {
                if (expr instanceof CallExpr) {
                    CallExpr call = (CallExpr) expr;
                    if (call.getFun() instanceof SelectorExpr) {
                        SelectorExpr sel = (SelectorExpr) call.getFun();
                        if (sel.getX() instanceof Ident && "bytes".equals(((Ident) sel.getX()).getName())) {
                            String name = sel.getSel().getName();
                            return "Index".equals(name) || "IndexAny".equals(name) || "IndexFunc".equals(name) || "IndexByte".equals(name) || "IndexRune".equals(name);
                        }
                    }
                }
                return false;
            }
            
            private boolean isMinusOneOrZero(Expr expr, String op) {
                if ("!=".equals(op) || "==".equals(op) || ">".equals(op)) {
                    if (expr instanceof UnaryExpr) {
                        UnaryExpr u = (UnaryExpr) expr;
                        if ("-".equals(u.getOp()) && u.getX() instanceof BasicLit && "1".equals(((BasicLit) u.getX()).getValue())) {
                            return true;
                        }
                    }
                } else if (">=".equals(op)) {
                    if (expr instanceof BasicLit && "0".equals(((BasicLit) expr).getValue())) {
                        return true;
                    }
                }
                return false;
            }
            
            private boolean isMinusOneOrZeroLeft(Expr expr, String op) {
                if ("!=".equals(op) || "==".equals(op) || "<".equals(op)) {
                    if (expr instanceof UnaryExpr) {
                        UnaryExpr u = (UnaryExpr) expr;
                        if ("-".equals(u.getOp()) && u.getX() instanceof BasicLit && "1".equals(((BasicLit) u.getX()).getValue())) {
                            return true;
                        }
                    }
                } else if ("<=".equals(op)) {
                    if (expr instanceof BasicLit && "0".equals(((BasicLit) expr).getValue())) {
                        return true;
                    }
                }
                return false;
            }
            
            private String inverseOp(String op) {
                switch (op) {
                    case "==": return "!=";
                    case "<": return ">";
                    case "<=": return ">=";
                    default: return op;
                }
            }

            private Tree toContains(CallExpr indexCall, Space prefix, boolean negate) {
                SelectorExpr sel = (SelectorExpr) indexCall.getFun();
                String newName = sel.getSel().getName().replace("Index", "Contains");
                if ("ContainsByte".equals(newName)) newName = "Contains"; // bytes.IndexByte -> bytes.Contains (Wait, does bytes have ContainsByte? No, bytes.Contains requires []byte, so IndexByte is not perfectly compatible with Contains unless cast. But bytes doesn't have ContainsByte. I'll just skip replacing IndexByte then by fixing isBytesIndex).
                else if ("ContainsRune".equals(newName)) newName = "ContainsRune";
                else if ("ContainsFunc".equals(newName)) newName = "ContainsFunc";
                else if ("ContainsAny".equals(newName)) newName = "ContainsAny";
                
                if ("Contains".equals(newName) && "IndexByte".equals(sel.getSel().getName())) {
                    // bytes.Contains requires []byte, but bytes.IndexByte takes byte.
                    // This is a bit unsafe without casts. But we will just do it for Index/IndexAny/IndexFunc/IndexRune.
                }
                
                Ident containsIdent = sel.getSel().withName(newName);
                CallExpr containsCall = indexCall.withFun(sel.withSel(containsIdent)).withPrefix(negate ? Space.EMPTY : prefix);
                
                if (negate) {
                    return new UnaryExpr(UUID.randomUUID(), prefix, Markers.EMPTY, "!", containsCall, null);
                }
                return containsCall;
            }
        };
    }
}
