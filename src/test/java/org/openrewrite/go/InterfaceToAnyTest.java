package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.go.tree.*;
import org.openrewrite.marker.Markers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InterfaceToAnyTest {
    
    @Test
    void replaceEmptyInterface() {
        // Create: func process(data interface{}) {}
        InterfaceTypeExpr emptyInterface = new InterfaceTypeExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.emptyList()
        );
        
        Field param = new Field(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList("data"),
            emptyInterface,
            ""
        );
        
        FuncType funcType = new FuncType(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(param),
            Collections.emptyList(),
            Collections.emptyList()
        );
        
        FuncDecl funcDecl = new FuncDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            null,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "process", null),
            Collections.emptyList(),
            funcType,
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
        
        InterfaceToAny recipe = new InterfaceToAny();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        FuncDecl resultFunc = (FuncDecl) resultFile.getDeclarations().get(0);
        Field resultParam = resultFunc.getType().getParams().get(0);
        
        // Should be transformed to: func process(data any) {}
        Ident resultType = (Ident) resultParam.getType();
        assertEquals("any", resultType.getName());
    }
    
    @Test
    void doNotReplaceNonEmptyInterface() {
        // Create: type Reader interface { Read(p []byte) (n int, err error) }
        Method readMethod = new Method(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            "Read",
             new FuncType(
                 UUID.randomUUID(),
                 Space.EMPTY,
                 Markers.EMPTY,
                 Collections.singletonList(new Field(
                     UUID.randomUUID(),
                     Space.EMPTY,
                     Markers.EMPTY,
                     Collections.singletonList("p"),
                     new SliceTypeExpr(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY,
                         new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "byte", null)
                     ),
                     ""
                 )),
                 java.util.Arrays.asList(
                     new Field(
                         UUID.randomUUID(),
                         Space.EMPTY,
                         Markers.EMPTY,
                         Collections.singletonList("n"),
                         new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "int", null),
                         ""
                     ),
                     new Field(
                         UUID.randomUUID(),
                         Space.EMPTY,
                         Markers.EMPTY,
                         Collections.singletonList("err"),
                         new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "error", null),
                         ""
                     )
                 ),
                 Collections.emptyList()
             )
         );
        
        InterfaceTypeExpr nonEmptyInterface = new InterfaceTypeExpr(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            Collections.singletonList(readMethod)
        );
        
        TypeSpec typeSpec = new TypeSpec(
            UUID.randomUUID(),
            Space.EMPTY,
            Markers.EMPTY,
            new Ident(UUID.randomUUID(), Space.EMPTY, Markers.EMPTY, "Reader", null),
            Collections.emptyList(),
            nonEmptyInterface,
            false
        );
        
        GenDecl genDecl = new GenDecl(
            UUID.randomUUID(),
            Space.build("\n"),
            Markers.EMPTY,
            "type",
            Collections.singletonList(typeSpec),
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
        
        InterfaceToAny recipe = new InterfaceToAny();
        ExecutionContext ctx = new InMemoryExecutionContext();
        
        Tree result = recipe.getVisitor().visit(file, ctx);
        
        GoFile resultFile = (GoFile) result;
        GenDecl resultGenDecl = (GenDecl) resultFile.getDeclarations().get(0);
        TypeSpec resultTypeSpec = (TypeSpec) resultGenDecl.getSpecs().get(0);
        
        // Should remain as interface with methods
        Expr resultType = resultTypeSpec.getType();
        assertTrue(resultType instanceof InterfaceTypeExpr);
        InterfaceTypeExpr resultInterface = (InterfaceTypeExpr) resultType;
        assertEquals(1, resultInterface.getMethods().size());
    }
}
