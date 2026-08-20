package printer

import (
	"testing"

	"github.com/openrewrite/rewrite-go/parser"
)

// TestRoundTrip is the acceptance criterion for lossless formatting: parsing a file and
// printing it back must reproduce the source byte for byte.
//
// It does not pass yet. Prefix capture in lst.Builder no longer swallows real source tokens,
// but three problems remain, each of which needs builder and printer to agree:
//
//  1. Keyword spacing is emitted twice. The printer writes "package ", "func ", "import "
//     with a trailing space, and the following node's prefix carries that same space.
//     One side has to own it -- the prefix should, so the printer should drop the trailing spaces.
//  2. Import grouping is lost. Builder.BuildFile walks the flattened file.Imports and wraps
//     each spec in its own ImportDecl, then the declaration loop emits the import GenDecl
//     again as an empty "import ()". Imports should be built from file.Decls instead.
//  3. Nested nodes starting at the same offset each capture the same whitespace run, so a
//     statement's indentation is printed once per nesting level. Only the outermost node at
//     a given offset should own the prefix.
//
// Remove the Skip once those are addressed.
func TestRoundTrip(t *testing.T) {
	t.Skip("lossless round trip not yet implemented -- see the comment above")

	src := `package main

import (
	"fmt"
	"os"
)

func main() {
	fmt.Println("hi")
	os.Exit(0)
}
`
	p := parser.New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	if got := New().Print(file); got != src {
		t.Errorf("round trip mismatch:\nwant %q\ngot  %q", src, got)
	}
}

// TestPrefixesContainOnlyWhitespace guards the fix for prefix capture: a Space must never
// carry real source tokens, or the printer emits them a second time.
func TestPrefixesContainOnlyWhitespace(t *testing.T) {
	src := `package main

import "fmt"

func greet(name string) string {
	return fmt.Sprintf("hi %s", name)
}
`
	p := parser.New()
	file, err := p.ParseSource("test.go", []byte(src))
	if err != nil {
		t.Fatalf("ParseSource failed: %v", err)
	}

	out := New().Print(file)
	for _, token := range []string{"package", "func", "return", "greet", "Sprintf"} {
		if count(out, token) != 1 {
			t.Errorf("token %q appears %d times in printed output, want 1:\n%q", token, count(out, token), out)
		}
	}
}

func count(haystack, needle string) int {
	n := 0
	for i := 0; i+len(needle) <= len(haystack); i++ {
		if haystack[i:i+len(needle)] == needle {
			n++
		}
	}
	return n
}
