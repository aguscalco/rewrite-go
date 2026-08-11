# Contributing to rewrite-go

Thank you for your interest in contributing to rewrite-go! This document provides guidelines and instructions for contributing.

## Getting Started

### Prerequisites

- Go 1.21 or later
- Java 21 or later
- Gradle 8+
- Protocol Buffers compiler (protoc)

### Setting Up the Development Environment

1. **Clone the repository**:
   ```bash
   git clone https://github.com/openrewrite/rewrite-go.git
   cd rewrite-go
   ```

2. **Generate protobuf code**:
   ```bash
   cd parser
   protoc --go_out=. --go_opt=paths=source_relative proto/go.proto
   ```

3. **Build the Go parser**:
   ```bash
   cd parser
   go mod download
   go build ./...
   ```

4. **Build the Java module**:
   ```bash
   cd ..
   ./gradlew build
   ```

## Project Structure

```
rewrite-go/
├── parser/                    # Go-native parser
│   ├── proto/                 # Protocol buffer definitions
│   ├── lst/                   # LST builder
│   └── printer/               # Go code printer
├── src/main/java/             # Java recipes and infrastructure
│   └── org/openrewrite/go/
│       ├── tree/              # LST classes
│       ├── internal/          # Deserialization
│       └── *.java             # Recipe implementations
└── src/test/                  # Tests
```

## Adding a New Recipe

### 1. Create the Recipe Class

Create a new Java class in `src/main/java/org/openrewrite/go/`:

```java
package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.go.tree.*;

public class MyRecipe extends Recipe {
    
    @Override
    public String getDisplayName() {
        return "My custom recipe";
    }
    
    @Override
    public String getDescription() {
        return "Description of what the recipe does.";
    }
    
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new GoVisitor<ExecutionContext>() {
            @Override
            public Tree visitFuncDecl(FuncDecl funcDecl, ExecutionContext ctx) {
                // Implement your transformation logic
                return funcDecl;
            }
        };
    }
}
```

### 2. Add Tests

Create a test class in `src/test/java/org/openrewrite/go/`:

```java
package org.openrewrite.go;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.go.Assertions.go;

class MyRecipeTest implements RewriteTest {
    
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MyRecipe());
    }
    
    @Test
    void myTestCase() {
        rewriteRun(
            go(
                """
                package main
                
                func example() {
                    // before
                }
                """,
                """
                package main
                
                func example() {
                    // after
                }
                """
            )
        );
    }
}
```

### 3. Update Documentation

Add your recipe to the README.md in the "Planned Recipes" section.

## Recipe Development Guidelines

### Using the Visitor Pattern

The `GoVisitor` class provides visit methods for all LST node types. Override the methods you need:

```java
@Override
public Tree visitCallExpr(CallExpr callExpr, ExecutionContext ctx) {
    // Check if this is the pattern you want to match
    if (isTargetCall(callExpr)) {
        // Transform and return the modified node
        return callExpr.withArgs(transformedArgs);
    }
    // Return unchanged
    return callExpr;
}
```

### Working with LST Nodes

All LST nodes are immutable. Use `with*` methods to create modified copies:

```java
// Change a function name
FuncDecl renamed = funcDecl.withName(newName);

// Add an import
GoFile withImport = goFile.withImports(newImports);

// Modify a statement
BlockStmt modified = blockStmt.withStmts(newStmts);
```

### Preserving Formatting

The `Space` class preserves whitespace and comments. When creating new nodes:

```java
// Preserve existing spacing
newNode.withPrefix(existingNode.getPrefix());

// Add specific spacing
newNode.withPrefix(Space.build("\n    "));
```

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific test:
```bash
./gradlew test --tests MyRecipeTest
```

## Code Style

### Java Code

- Follow OpenRewrite conventions
- Use Lombok annotations (@Data, @With, etc.)
- Keep recipes focused on a single transformation
- Add comprehensive Javadoc

### Go Code

- Follow standard Go conventions
- Use `gofmt` for formatting
- Add tests for parser functionality

## Submitting Changes

1. **Fork the repository**
2. **Create a feature branch**:
   ```bash
   git checkout -b feature/my-new-recipe
   ```
3. **Make your changes**
4. **Add tests** for new functionality
5. **Ensure all tests pass**:
   ```bash
   ./gradlew test
   ```
6. **Commit your changes**:
   ```bash
   git commit -am "Add my new recipe"
   ```
7. **Push to your fork**:
   ```bash
   git push origin feature/my-new-recipe
   ```
8. **Create a Pull Request**

## Pull Request Guidelines

- Provide a clear description of the changes
- Include before/after examples for recipes
- Reference any related issues
- Ensure CI passes
- Be responsive to review comments

## Reporting Issues

When reporting issues, please include:

- Go version
- Java version
- Gradle version
- Example code that demonstrates the issue
- Expected vs actual behavior

## Getting Help

- Join the [OpenRewrite Slack](https://join.slack.com/t/rewriteoss/shared_invite/)
- Ask questions in GitHub Discussions
- Check existing issues and documentation

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

Thank you for contributing to rewrite-go! 🎉
