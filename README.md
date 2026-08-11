# rewrite-go

OpenRewrite recipes for Go source code refactoring and migration.

## Overview

This project provides OpenRewrite support for Go programming language, enabling automated refactoring, code migrations, and static analysis for Go codebases.

## Architecture

The project uses a **Go-native parser** with **Protocol Buffers** for LST (Lossless Semantic Tree) serialization:

```
rewrite-go/
├── parser/                    # Go-native parser module
│   ├── go.mod                 # Go module definition
│   ├── parser.go              # Main parser (uses go/parser + go/types)
│   ├── proto/
│   │   └── go.proto           # Protocol buffer schema for Go LST
│   ├── lst/
│   │   └── builder.go         # Converts Go AST to proto LST
│   └── printer/
│       └── printer.go         # Converts LST back to Go source
├── src/main/
│   ├── java/org/openrewrite/go/
│   │   ├── tree/              # Java LST classes (generated from proto)
│   │   ├── internal/          # Deserialization, visitors
│   │   └── search/            # Search recipes
│   └── resources/META-INF/rewrite/
│       └── *.yml              # Declarative YAML recipes
└── src/test/                  # Tests
```

## Features

### Planned Recipes

- **Import organization**: Sort and group imports (stdlib vs third-party)
- **Error handling**: Wrap errors with context, use `errors.Is`/`errors.As`
- **Stdlib migrations**: `io/ioutil` → `io`, context propagation
- **Go modules cleanup**: Remove unused dependencies
- **Code modernization**: Update to latest Go idioms

### Future: rewrite-gin

A separate module for Gin framework migrations:
- Gin version migrations
- Middleware pattern updates
- Context method changes
- Routing API updates

## Building

### Prerequisites

- Go 1.21+
- Java 21+
- Gradle 8+

### Build Steps

1. **Generate protobuf code**:
   ```bash
   cd parser
   protoc --go_out=. --go_opt=paths=source_relative proto/go.proto
   ```

2. **Build Go parser**:
   ```bash
   cd parser
   go build ./...
   ```

3. **Build Java module**:
   ```bash
   ./gradlew build
   ```

## Usage

### Running Recipes

With Gradle:
```gradle
plugins {
    id("org.openrewrite.rewrite") version "latest.release"
}

dependencies {
    rewrite("org.openrewrite.recipe:rewrite-go:0.1.0-SNAPSHOT")
}

rewrite {
    activeRecipe("org.openrewrite.go.OrganizeImports")
}
```

With Maven:
```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>latest.release</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.go.OrganizeImports</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-go</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
</plugin>
```

## Development

### Adding a New Recipe

1. Create a Java class in `src/main/java/org/openrewrite/go/`:
   ```java
   public class MyRecipe extends Recipe {
       @Override
       public String getDisplayName() {
           return "My custom recipe";
       }
       
       @Override
       public TreeVisitor<?, ExecutionContext> getVisitor() {
           return new GoVisitor<ExecutionContext>() {
               // Implement visitor logic
           };
       }
   }
   ```

2. Or create a declarative YAML recipe in `src/main/resources/META-INF/rewrite/`:
   ```yaml
   ---
   type: specs.openrewrite.org/v1beta/recipe
   name: org.openrewrite.go.MyRecipe
   displayName: My custom recipe
   description: Description of what the recipe does
   recipeList:
     - org.openrewrite.go.SomeOtherRecipe
   ```

3. Add tests in `src/test/java/org/openrewrite/go/`

### Testing

```bash
./gradlew test
```

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Related Projects

- [OpenRewrite](https://github.com/openrewrite/rewrite) - Core refactoring engine
- [rewrite-spring](https://github.com/openrewrite/rewrite-spring) - Spring framework recipes
- [rewrite-testing-frameworks](https://github.com/openrewrite/rewrite-testing-frameworks) - Testing framework recipes

## Roadmap

- [ ] Complete Go LST proto schema
- [ ] Implement full Go parser coverage
- [ ] Build Java LST classes from proto
- [ ] Create core recipes (imports, error handling)
- [ ] Add comprehensive test suite
- [ ] Publish to Maven Central
- [ ] Start rewrite-gin module

## Status

🚧 **Early Development** - This project is in active development and not yet ready for production use.
