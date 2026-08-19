package org.openrewrite.go.tree;

import org.junit.jupiter.api.Test;
import org.openrewrite.PrintOutputCapture;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GoPrinterTest {
    
    @Test
    void printEmptyFile() {
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main", capture.getOut());
    }
    
    @Test
    void printFunctionDeclaration() {
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "hello", null),
            Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            new BlockStmt(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, Collections.emptyList(), Space.EMPTY)
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nfunc hello()  {}", capture.getOut());
    }
    
    @Test
    void printImportDeclaration() {
        ImportSpec spec = new ImportSpec(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            null,
            new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"fmt\"")
        );
        
        ImportDecl importDecl = new ImportDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            Collections.singletonList(spec),
            false,
            Space.EMPTY
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.singletonList(importDecl),
            Collections.emptyList(),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nimport \n\t\"fmt\"", capture.getOut());
    }
    
    @Test
    void printVariableDeclaration() {
        ValueSpec valueSpec = new ValueSpec(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "x", null)),
            new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "int", null),
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "INT", "42"))
        );
        
        GenDecl genDecl = new GenDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            "var",
            Collections.singletonList(valueSpec),
            false,
            Space.EMPTY
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(genDecl),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nvar x  int =  42", capture.getOut());
    }
    
    @Test
    void printCallExpression() {
        SelectorExpr selector = new SelectorExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "fmt", null),
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Println", null),
            null
        );
        
        CallExpr callExpr = new CallExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            selector,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "STRING", "\"hello\"")),
            false,
            null
        );
        
        ExprStmt exprStmt = new ExprStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            callExpr
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(exprStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null),
            Collections.emptyList(),
            new FuncType(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            body
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nfunc main()  {\n\tfmt.Println(\"hello\")}", capture.getOut());
    }
    
    @Test
    void printReturnStatement() {
        ReturnStmt returnStmt = new ReturnStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(new BasicLit(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "INT", "42"))
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(returnStmt),
            Space.EMPTY
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "getNumber", null),
            Collections.emptyList(),
            new FuncType(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.emptyList(),
                Collections.singletonList(new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                    ""
                )),
                Collections.emptyList()
            ),
            body
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nfunc getNumber() int  {\n\treturn 42}", capture.getOut());
    }
    
    @Test
    void printBinaryExpression() {
        BinaryExpr binaryExpr = new BinaryExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "a", null),
            "+",
            new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "b", null),
            null
        );
        
        ReturnStmt returnStmt = new ReturnStmt(
            UUID.randomUUID(),
            Space.build("\n\t"),
            Markers.EMPTY,
            Collections.singletonList(binaryExpr)
        );
        
        BlockStmt body = new BlockStmt(
            UUID.randomUUID(),
            Space.build(" "),
            Markers.EMPTY,
            Collections.singletonList(returnStmt),
            Space.EMPTY
        );
        
        Field param = new Field(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Arrays.asList("a", "b"),
            new Ident(UUID.randomUUID(), Space.build(" "), Markers.EMPTY, "int", null),
            ""
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "add", null),
            Collections.emptyList(),
            new FuncType(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                Collections.singletonList(param),
                Collections.singletonList(new Field(
                    UUID.randomUUID(),
                    Space.EMPTY,
                    Markers.EMPTY,
                    Collections.emptyList(),
                    new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                    ""
                )),
                Collections.emptyList()
            ),
            body
        );
        
        GoFile file = new GoFile(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Paths.get("test.go"),
            StandardCharsets.UTF_8,
            false,
            new PackageClause(
                UUID.randomUUID(),
                Space.EMPTY,
                Markers.EMPTY,
                new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "main", null)
            ),
            Collections.emptyList(),
            Collections.singletonList(funcDecl),
            Space.EMPTY,
            null,
            null
        );
        
        GoPrinter<Integer> printer = new GoPrinter<>();
        PrintOutputCapture<Integer> capture = new PrintOutputCapture<>(0);
        printer.visit(file, capture);
        
        assertEquals("package main\nfunc add(a, b  int) int  {\n\treturn a +  b}", capture.getOut());
    }
}
