package printer

import (
	"testing"

	"github.com/openrewrite/rewrite-go/parser"
)

// TestRoundTrip is the acceptance criterion for lossless formatting: parsing a file and
// printing it back must reproduce the source byte for byte.
//
// Prefixes carry every gap between tokens, and lst.Builder keeps a cursor so a token is
// accounted for exactly once. Anything the printer writes literally -- keywords,
// punctuation, separators -- the builder must consume, or it leaks into the next prefix
// and gets printed twice. Add a case here whenever you teach the builder a new construct.
func TestRoundTrip(t *testing.T) {
	cases := []struct {
		name string
		src  string
	}{
		{"package only", "package main\n"},
		{"single import", "package main\n\nimport \"fmt\"\n"},
		{"grouped imports", "package main\n\nimport (\n\t\"fmt\"\n\t\"os\"\n)\n"},
		{"aliased import", "package main\n\nimport (\n\tf \"fmt\"\n)\n"},
		{"empty func", "package main\n\nfunc main() {\n}\n"},
		{"calls", "package main\n\nfunc main() {\n\tfmt.Println(\"hi\")\n\tos.Exit(0)\n}\n"},
		{"params and result", "package main\n\nfunc greet(name string) string {\n\treturn name\n}\n"},
		{"multiple params", "package main\n\nfunc add(a int, b int) int {\n\treturn a\n}\n"},
		{"multi arg call", "package main\n\nfunc main() {\n\tfmt.Sprintf(\"%s %s\", a, b)\n}\n"},
		{"assignment", "package main\n\nfunc main() {\n\tx := compute()\n}\n"},
		{"var decl", "package main\n\nvar x int\n"},
		{"grouped var decl", "package main\n\nvar (\n\tx int\n\ty string\n)\n"},
		{"method", "package main\n\nfunc (p Point) X() int {\n\treturn p.x\n}\n"},
		{"if statement", "package main\n\nfunc main() {\n\tif err != nil {\n\t\treturn\n\t}\n}\n"},
		{"blank lines preserved", "package main\n\n\nfunc main() {\n\n\tx := 1\n\n}\n"},
	}

	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			file, err := parser.New().ParseSource("test.go", []byte(tc.src))
			if err != nil {
				t.Fatalf("ParseSource failed: %v", err)
			}
			if got := New().Print(file); got != tc.src {
				t.Errorf("round trip mismatch:\nwant %q\ngot  %q", tc.src, got)
			}
		})
	}
}

// TestRoundTripKnownGaps records constructs that do not round trip yet, so the gap is
// discoverable rather than folklore. Each needs a schema change: StructTypeExpr and
// InterfaceTypeExpr have no "end" Space field, so the gap before their closing brace has
// nowhere to live and is dropped. Adding the field means regenerating both bindings and
// widening the Java LST to match.
func TestRoundTripKnownGaps(t *testing.T) {
	t.Skip("needs an `end` Space on StructTypeExpr and InterfaceTypeExpr")

	cases := []string{
		"package main\n\ntype Point struct{}\n",
		"package main\n\ntype Point struct {\n\tx int\n}\n",
		"package main\n\ntype Reader interface {\n\tRead() error\n}\n",
	}

	for _, src := range cases {
		file, err := parser.New().ParseSource("test.go", []byte(src))
		if err != nil {
			t.Fatalf("ParseSource failed: %v", err)
		}
		if got := New().Print(file); got != src {
			t.Errorf("round trip mismatch:\nwant %q\ngot  %q", src, got)
		}
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
