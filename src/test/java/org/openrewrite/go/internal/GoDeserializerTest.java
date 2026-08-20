package org.openrewrite.go.internal;

import org.junit.jupiter.api.Test;
import org.openrewrite.go.proto.GoProto;
import org.openrewrite.go.tree.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GoDeserializerTest {
    
    @Test
    void deserializeEmptyFile() {
        GoProto.GoFile proto = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .setCharsetBomMarked(false)
            .setPackageClause(GoProto.PackageClause.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("main")
                    .build())
                .build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoDeserializer deserializer = new GoDeserializer();
        GoFile file = deserializer.deserialize(proto);
        
        assertNotNull(file);
        assertEquals("test.go", file.getSourcePath().toString());
        assertNotNull(file.getPackageClause());
        assertEquals("main", file.getPackageClause().getName().getName());
        assertTrue(file.getImports().isEmpty());
        assertTrue(file.getDeclarations().isEmpty());
    }
    
    @Test
    void deserializeFunctionDeclaration() {
        GoProto.FuncDecl funcProto = GoProto.FuncDecl.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setName(GoProto.Ident.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName("hello")
                .build())
            .setType(GoProto.FuncType.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .build())
            .setBody(GoProto.BlockStmt.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace(" ").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setEnd(GoProto.Space.newBuilder().setWhitespace("").build())
                .build())
            .build();
        
        GoProto.GoFile proto = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .setCharsetBomMarked(false)
            .setPackageClause(GoProto.PackageClause.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("main")
                    .build())
                .build())
            .addDeclarations(GoProto.Decl.newBuilder().setFuncDecl(funcProto).build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoDeserializer deserializer = new GoDeserializer();
        GoFile file = deserializer.deserialize(proto);
        
        assertEquals(1, file.getDeclarations().size());
        FuncDecl funcDecl = (FuncDecl) file.getDeclarations().get(0);
        assertEquals("hello", funcDecl.getName().getName());
        assertNotNull(funcDecl.getType());
        assertNotNull(funcDecl.getBody());
    }
    
    @Test
    void deserializeImportDeclaration() {
        GoProto.ImportSpec specProto = GoProto.ImportSpec.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n\t").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setPath(GoProto.BasicLit.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setKind("STRING")
                .setValue("\"fmt\"")
                .build())
            .build();
        
        GoProto.ImportDecl importProto = GoProto.ImportDecl.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .addSpecs(specProto)
            .setGrouped(false)
            .setEnd(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoProto.GoFile proto = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .setCharsetBomMarked(false)
            .setPackageClause(GoProto.PackageClause.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("main")
                    .build())
                .build())
            .addImports(importProto)
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoDeserializer deserializer = new GoDeserializer();
        GoFile file = deserializer.deserialize(proto);
        
        assertEquals(1, file.getImports().size());
        ImportDecl importDecl = file.getImports().get(0);
        assertEquals(1, importDecl.getSpecs().size());
        assertEquals("\"fmt\"", importDecl.getSpecs().get(0).getPath().getValue());
        assertFalse(importDecl.isGrouped());
    }
    
    @Test
    void deserializeVariableDeclaration() {
        GoProto.ValueSpec valueSpecProto = GoProto.ValueSpec.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .addNames(GoProto.Ident.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName("x")
                .build())
            .setType(GoProto.Expr.newBuilder()
                .setIdent(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace(" ").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("int")
                    .build())
                .build())
            .addValues(GoProto.Expr.newBuilder()
                .setBasicLit(GoProto.BasicLit.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace(" ").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setKind("INT")
                    .setValue("42")
                    .build())
                .build())
            .build();
        
        GoProto.GenDecl genDeclProto = GoProto.GenDecl.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setTok("var")
            .addSpecs(GoProto.Spec.newBuilder().setValueSpec(valueSpecProto).build())
            .setGrouped(false)
            .setEnd(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoProto.GoFile proto = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .setCharsetBomMarked(false)
            .setPackageClause(GoProto.PackageClause.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("main")
                    .build())
                .build())
            .addDeclarations(GoProto.Decl.newBuilder().setGenDecl(genDeclProto).build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoDeserializer deserializer = new GoDeserializer();
        GoFile file = deserializer.deserialize(proto);
        
        assertEquals(1, file.getDeclarations().size());
        GenDecl genDecl = (GenDecl) file.getDeclarations().get(0);
        assertEquals("var", genDecl.getTok());
        assertEquals(1, genDecl.getSpecs().size());
        
        ValueSpec valueSpec = (ValueSpec) genDecl.getSpecs().get(0);
        assertEquals(1, valueSpec.getNames().size());
        assertEquals("x", valueSpec.getNames().get(0).getName());
        assertNotNull(valueSpec.getType());
        assertEquals(1, valueSpec.getValues().size());
    }
    
    @Test
    void deserializeCallExpression() {
        GoProto.SelectorExpr selectorProto = GoProto.SelectorExpr.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setX(GoProto.Expr.newBuilder()
                .setIdent(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("fmt")
                    .build())
                .build())
            .setSel(GoProto.Ident.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName("Println")
                .build())
            .build();
        
        GoProto.CallExpr callProto = GoProto.CallExpr.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setFun(GoProto.Expr.newBuilder().setSelectorExpr(selectorProto).build())
            .addArgs(GoProto.Expr.newBuilder()
                .setBasicLit(GoProto.BasicLit.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setKind("STRING")
                    .setValue("\"hello\"")
                    .build())
                .build())
            .setEllipsis(false)
            .build();
        
        GoProto.ExprStmt exprStmtProto = GoProto.ExprStmt.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n\t").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setExpr(GoProto.Expr.newBuilder().setCallExpr(callProto).build())
            .build();
        
        GoProto.BlockStmt bodyProto = GoProto.BlockStmt.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace(" ").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .addStmts(GoProto.Stmt.newBuilder().setExprStmt(exprStmtProto).build())
            .setEnd(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoProto.FuncDecl funcProto = GoProto.FuncDecl.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("\n").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setName(GoProto.Ident.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName("main")
                .build())
            .setType(GoProto.FuncType.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .build())
            .setBody(bodyProto)
            .build();
        
        GoProto.GoFile proto = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .setCharsetBomMarked(false)
            .setPackageClause(GoProto.PackageClause.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(GoProto.Ident.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName("main")
                    .build())
                .build())
            .addDeclarations(GoProto.Decl.newBuilder().setFuncDecl(funcProto).build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
        
        GoDeserializer deserializer = new GoDeserializer();
        GoFile file = deserializer.deserialize(proto);
        
        FuncDecl funcDecl = (FuncDecl) file.getDeclarations().get(0);
        assertEquals(1, funcDecl.getBody().getStmts().size());
        
        ExprStmt exprStmt = (ExprStmt) funcDecl.getBody().getStmts().get(0);
        CallExpr callExpr = (CallExpr) exprStmt.getExpr();
        
        SelectorExpr selectorExpr = (SelectorExpr) callExpr.getFun();
        Ident pkgIdent = (Ident) selectorExpr.getX();
        assertEquals("fmt", pkgIdent.getName());
        assertEquals("Println", selectorExpr.getSel().getName());
        
        assertEquals(1, callExpr.getArgs().size());
        BasicLit arg = (BasicLit) callExpr.getArgs().get(0);
        assertEquals("\"hello\"", arg.getValue());
    }
    
    private GoProto.UUID newUUID() {
        UUID uuid = UUID.randomUUID();
        return GoProto.UUID.newBuilder()
            .setMostSigBits(uuid.getMostSignificantBits())
            .setLeastSigBits(uuid.getLeastSignificantBits())
            .build();
    }

    private GoProto.Expr identExpr(String name) {
        return GoProto.Expr.newBuilder()
            .setIdent(GoProto.Ident.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setName(name)
                .build())
            .build();
    }

    @Test
    void deserializeSliceTypeExpr() {
        GoProto.Expr proto = GoProto.Expr.newBuilder()
            .setSliceTypeExpr(GoProto.SliceTypeExpr.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setElt(identExpr("string"))
                .build())
            .build();

        Expr result = deserializeExprViaValueSpec(proto);
        assertInstanceOf(SliceTypeExpr.class, result);
        assertEquals("string", ((Ident) ((SliceTypeExpr) result).getElt()).getName());
    }

    @Test
    void deserializePointerTypeExpr() {
        GoProto.Expr proto = GoProto.Expr.newBuilder()
            .setPointerTypeExpr(GoProto.PointerTypeExpr.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .setBase(identExpr("MyError"))
                .build())
            .build();

        Expr result = deserializeExprViaValueSpec(proto);
        assertInstanceOf(PointerTypeExpr.class, result);
        assertEquals("MyError", ((Ident) ((PointerTypeExpr) result).getBase()).getName());
    }

    /**
     * A proto node with no counterpart in org.openrewrite.go.tree must fail loudly. Silently
     * dropping it would corrupt the LST -- a for loop would simply vanish from the file.
     */
    @Test
    void unsupportedStmtKindThrowsRatherThanBeingDropped() {
        GoProto.Stmt proto = GoProto.Stmt.newBuilder()
            .setForStmt(GoProto.ForStmt.newBuilder()
                .setId(newUUID())
                .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                .build())
            .build();

        GoProto.GoFile file = fileWithFuncBody(proto);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> new GoDeserializer().deserialize(file));
        assertTrue(e.getMessage().contains("FOR_STMT"), e.getMessage());
    }

    private Expr deserializeExprViaValueSpec(GoProto.Expr typeExpr) {
        GoProto.GoFile file = GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .addDeclarations(GoProto.Decl.newBuilder()
                .setGenDecl(GoProto.GenDecl.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setTok("var")
                    .addSpecs(GoProto.Spec.newBuilder()
                        .setValueSpec(GoProto.ValueSpec.newBuilder()
                            .setId(newUUID())
                            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                            .setType(typeExpr)
                            .build())
                        .build())
                    .build())
                .build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();

        GoFile go = new GoDeserializer().deserialize(file);
        GenDecl decl = (GenDecl) go.getDeclarations().get(0);
        return ((ValueSpec) decl.getSpecs().get(0)).getType();
    }

    private GoProto.GoFile fileWithFuncBody(GoProto.Stmt stmt) {
        return GoProto.GoFile.newBuilder()
            .setId(newUUID())
            .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
            .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
            .setSourcePath("test.go")
            .setCharsetName("UTF-8")
            .addDeclarations(GoProto.Decl.newBuilder()
                .setFuncDecl(GoProto.FuncDecl.newBuilder()
                    .setId(newUUID())
                    .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                    .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                    .setName(GoProto.Ident.newBuilder()
                        .setId(newUUID())
                        .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                        .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                        .setName("main")
                        .build())
                    .setBody(GoProto.BlockStmt.newBuilder()
                        .setId(newUUID())
                        .setPrefix(GoProto.Space.newBuilder().setWhitespace("").build())
                        .setMarkers(GoProto.Markers.newBuilder().setId(newUUID()).build())
                        .addStmts(stmt)
                        .build())
                    .build())
                .build())
            .setEof(GoProto.Space.newBuilder().setWhitespace("").build())
            .build();
    }
}
