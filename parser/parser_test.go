package parser

import (
	"testing"

	"github.com/openrewrite/rewrite-go/parser/proto"
)

func TestParseEmptyFile(t *testing.T) {
	src := `package main`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if file == nil {
		t.Fatal("Expected non-nil file")
	}

	if file.PackageClause == nil {
		t.Fatal("Expected package clause")
	}

	if file.PackageClause.Name.Name != "main" {
		t.Errorf("Expected package name 'main', got '%s'", file.PackageClause.Name.Name)
	}

	if len(file.Imports) != 0 {
		t.Errorf("Expected 0 imports, got %d", len(file.Imports))
	}

	if len(file.Declarations) != 0 {
		t.Errorf("Expected 0 declarations, got %d", len(file.Declarations))
	}
}

func TestParseSimpleFunction(t *testing.T) {
	src := `package main

func hello() {
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if len(file.Declarations) != 1 {
		t.Fatalf("Expected 1 declaration, got %d", len(file.Declarations))
	}

	decl := file.Declarations[0]
	funcDecl := decl.GetFuncDecl()
	if funcDecl == nil {
		t.Fatal("Expected function declaration")
	}

	if funcDecl.Name.Name != "hello" {
		t.Errorf("Expected function name 'hello', got '%s'", funcDecl.Name.Name)
	}

	if funcDecl.Body == nil {
		t.Fatal("Expected function body")
	}

	if len(funcDecl.Body.Stmts) != 0 {
		t.Errorf("Expected 0 statements in body, got %d", len(funcDecl.Body.Stmts))
	}
}

func TestParseFunctionWithParams(t *testing.T) {
	src := `package main

func add(a, b int) int {
	return a + b
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	if funcDecl == nil {
		t.Fatal("Expected function declaration")
	}

	if funcDecl.Type == nil {
		t.Fatal("Expected function type")
	}

	if len(funcDecl.Type.Params) != 1 {
		t.Fatalf("Expected 1 parameter field, got %d", len(funcDecl.Type.Params))
	}

	param := funcDecl.Type.Params[0]
	if len(param.Names) != 2 {
		t.Errorf("Expected 2 parameter names, got %d", len(param.Names))
	}

	if param.Names[0] != "a" || param.Names[1] != "b" {
		t.Errorf("Expected params [a, b], got %v", param.Names)
	}

	if len(funcDecl.Type.Results) != 1 {
		t.Errorf("Expected 1 result, got %d", len(funcDecl.Type.Results))
	}

	if len(funcDecl.Body.Stmts) != 1 {
		t.Errorf("Expected 1 statement in body, got %d", len(funcDecl.Body.Stmts))
	}
}

func TestParseImports(t *testing.T) {
	src := `package main

import (
	"fmt"
	"os"
)`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if len(file.Imports) != 2 {
		t.Fatalf("Expected 2 imports, got %d", len(file.Imports))
	}

	imp1 := file.Imports[0]
	if len(imp1.Specs) != 1 {
		t.Fatalf("Expected 1 spec in first import, got %d", len(imp1.Specs))
	}

	if imp1.Specs[0].Path.Value != `"fmt"` {
		t.Errorf("Expected first import 'fmt', got %s", imp1.Specs[0].Path.Value)
	}

	imp2 := file.Imports[1]
	if imp2.Specs[0].Path.Value != `"os"` {
		t.Errorf("Expected second import 'os', got %s", imp2.Specs[0].Path.Value)
	}
}

func TestParseVariableDeclaration(t *testing.T) {
	src := `package main

var x int = 42`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if len(file.Declarations) != 1 {
		t.Fatalf("Expected 1 declaration, got %d", len(file.Declarations))
	}

	genDecl := file.Declarations[0].GetGenDecl()
	if genDecl == nil {
		t.Fatal("Expected gen declaration")
	}

	if genDecl.Tok != "var" {
		t.Errorf("Expected token 'var', got '%s'", genDecl.Tok)
	}

	if len(genDecl.Specs) != 1 {
		t.Fatalf("Expected 1 spec, got %d", len(genDecl.Specs))
	}

	valueSpec := genDecl.Specs[0].GetValueSpec()
	if valueSpec == nil {
		t.Fatal("Expected value spec")
	}

	if len(valueSpec.Names) != 1 {
		t.Errorf("Expected 1 name, got %d", len(valueSpec.Names))
	}

	if valueSpec.Names[0].Name != "x" {
		t.Errorf("Expected name 'x', got '%s'", valueSpec.Names[0].Name)
	}

	if len(valueSpec.Values) != 1 {
		t.Errorf("Expected 1 value, got %d", len(valueSpec.Values))
	}
}

func TestParseTypeDeclaration(t *testing.T) {
	src := `package main

type Point struct {
	X int
	Y int
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	genDecl := file.Declarations[0].GetGenDecl()
	if genDecl == nil {
		t.Fatal("Expected gen declaration")
	}

	if genDecl.Tok != "type" {
		t.Errorf("Expected token 'type', got '%s'", genDecl.Tok)
	}

	typeSpec := genDecl.Specs[0].GetTypeSpec()
	if typeSpec == nil {
		t.Fatal("Expected type spec")
	}

	if typeSpec.Name.Name != "Point" {
		t.Errorf("Expected type name 'Point', got '%s'", typeSpec.Name.Name)
	}
}

func TestParseMethodCall(t *testing.T) {
	src := `package main

import "fmt"

func main() {
	fmt.Println("hello")
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	// Find the function declaration (skip imports)
	var funcDecl *proto.FuncDecl
	for _, decl := range file.Declarations {
		if fd := decl.GetFuncDecl(); fd != nil {
			funcDecl = fd
			break
		}
	}

	if funcDecl == nil {
		t.Fatal("Expected function declaration")
	}

	if len(funcDecl.Body.Stmts) != 1 {
		t.Fatalf("Expected 1 statement, got %d", len(funcDecl.Body.Stmts))
	}

	exprStmt := funcDecl.Body.Stmts[0].GetExprStmt()
	if exprStmt == nil {
		t.Fatal("Expected expression statement")
	}

	callExpr := exprStmt.Expr.GetCallExpr()
	if callExpr == nil {
		t.Fatal("Expected call expression")
	}

	selectorExpr := callExpr.Fun.GetSelectorExpr()
	if selectorExpr == nil {
		t.Fatal("Expected selector expression")
	}

	if selectorExpr.Sel.Name != "Println" {
		t.Errorf("Expected method name 'Println', got '%s'", selectorExpr.Sel.Name)
	}

	if len(callExpr.Args) != 1 {
		t.Errorf("Expected 1 argument, got %d", len(callExpr.Args))
	}
}

func TestParseReturnStatement(t *testing.T) {
	src := `package main

func getNumber() int {
	return 42
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	if len(funcDecl.Body.Stmts) != 1 {
		t.Fatalf("Expected 1 statement, got %d", len(funcDecl.Body.Stmts))
	}

	returnStmt := funcDecl.Body.Stmts[0].GetReturnStmt()
	if returnStmt == nil {
		t.Fatal("Expected return statement")
	}

	if len(returnStmt.Results) != 1 {
		t.Errorf("Expected 1 result, got %d", len(returnStmt.Results))
	}

	basicLit := returnStmt.Results[0].GetBasicLit()
	if basicLit == nil {
		t.Fatal("Expected basic literal")
	}

	if basicLit.Value != "42" {
		t.Errorf("Expected value '42', got '%s'", basicLit.Value)
	}
}

func TestParseBinaryExpression(t *testing.T) {
	src := `package main

func add(a, b int) int {
	return a + b
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	returnStmt := funcDecl.Body.Stmts[0].GetReturnStmt()

	binaryExpr := returnStmt.Results[0].GetBinaryExpr()
	if binaryExpr == nil {
		t.Fatal("Expected binary expression")
	}

	if binaryExpr.Op != "+" {
		t.Errorf("Expected operator '+', got '%s'", binaryExpr.Op)
	}

	leftIdent := binaryExpr.X.GetIdent()
	if leftIdent == nil || leftIdent.Name != "a" {
		t.Errorf("Expected left operand 'a'")
	}

	rightIdent := binaryExpr.Y.GetIdent()
	if rightIdent == nil || rightIdent.Name != "b" {
		t.Errorf("Expected right operand 'b'")
	}
}

func TestParseAssignment(t *testing.T) {
	src := `package main

func main() {
	x := 10
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	assignStmt := funcDecl.Body.Stmts[0].GetAssignStmt()

	if assignStmt == nil {
		t.Fatal("Expected assignment statement")
	}

	if assignStmt.Tok != ":=" {
		t.Errorf("Expected token ':=', got '%s'", assignStmt.Tok)
	}

	if len(assignStmt.Lhs) != 1 {
		t.Errorf("Expected 1 left operand, got %d", len(assignStmt.Lhs))
	}

	if len(assignStmt.Rhs) != 1 {
		t.Errorf("Expected 1 right operand, got %d", len(assignStmt.Rhs))
	}
}

func TestParseIfStatement(t *testing.T) {
	src := `package main

func check(x int) {
	if x > 0 {
		return
	}
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	ifStmt := funcDecl.Body.Stmts[0].GetIfStmt()

	if ifStmt == nil {
		t.Fatal("Expected if statement")
	}

	if ifStmt.Cond == nil {
		t.Fatal("Expected condition")
	}

	binaryExpr := ifStmt.Cond.GetBinaryExpr()
	if binaryExpr == nil {
		t.Fatal("Expected binary expression in condition")
	}

	if binaryExpr.Op != ">" {
		t.Errorf("Expected operator '>', got '%s'", binaryExpr.Op)
	}

	if ifStmt.Body == nil {
		t.Fatal("Expected body")
	}

	if len(ifStmt.Body.Stmts) != 1 {
		t.Errorf("Expected 1 statement in body, got %d", len(ifStmt.Body.Stmts))
	}
}

func TestParseForLoop(t *testing.T) {
	src := `package main

func count() {
	for i := 0; i < 10; {
	}
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	forStmt := funcDecl.Body.Stmts[0].GetForStmt()

	if forStmt == nil {
		t.Fatal("Expected for statement")
	}

	if forStmt.Init == nil {
		t.Fatal("Expected init statement")
	}

	if forStmt.Cond == nil {
		t.Fatal("Expected condition")
	}

	// Post statement is optional (i++ not supported yet)

	if forStmt.Body == nil {
		t.Fatal("Expected body")
	}
}

func TestParseRangeLoop(t *testing.T) {
	src := `package main

func iterate(items []int) {
	for i, v := range items {
	}
}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	funcDecl := file.Declarations[0].GetFuncDecl()
	rangeStmt := funcDecl.Body.Stmts[0].GetRangeStmt()

	if rangeStmt == nil {
		t.Fatal("Expected range statement")
	}

	if rangeStmt.Key == nil {
		t.Fatal("Expected key")
	}

	if rangeStmt.Value == nil {
		t.Fatal("Expected value")
	}

	if rangeStmt.X == nil {
		t.Fatal("Expected range expression")
	}

	if rangeStmt.Tok != ":=" {
		t.Errorf("Expected token ':=', got '%s'", rangeStmt.Tok)
	}
}

func TestParseSyntaxError(t *testing.T) {
	src := `package main

func broken( {
}`

	p := New()
	_, err := p.ParseSource("test.go", []byte(src))

	if err == nil {
		t.Fatal("Expected parse error for invalid syntax")
	}
}

func TestParseMultipleFunctions(t *testing.T) {
	src := `package main

func foo() {}

func bar() {}`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if len(file.Declarations) != 2 {
		t.Fatalf("Expected 2 declarations, got %d", len(file.Declarations))
	}

	func1 := file.Declarations[0].GetFuncDecl()
	func2 := file.Declarations[1].GetFuncDecl()

	if func1.Name.Name != "foo" {
		t.Errorf("Expected first function 'foo', got '%s'", func1.Name.Name)
	}

	if func2.Name.Name != "bar" {
		t.Errorf("Expected second function 'bar', got '%s'", func2.Name.Name)
	}
}

func TestParseImportWithAlias(t *testing.T) {
	src := `package main

import f "fmt"`

	p := New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if len(file.Imports) != 1 {
		t.Fatalf("Expected 1 import, got %d", len(file.Imports))
	}

	imp := file.Imports[0]
	if len(imp.Specs) != 1 {
		t.Fatalf("Expected 1 spec, got %d", len(imp.Specs))
	}

	spec := imp.Specs[0]
	if spec.Alias == nil {
		t.Fatal("Expected alias")
	}

	if spec.Alias.Name != "f" {
		t.Errorf("Expected alias 'f', got '%s'", spec.Alias.Name)
	}

	if spec.Path.Value != `"fmt"` {
		t.Errorf("Expected path 'fmt', got %s", spec.Path.Value)
	}
}
