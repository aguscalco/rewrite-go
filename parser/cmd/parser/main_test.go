package main

import (
	"bytes"
	"encoding/binary"
	"io"
	"testing"

	"google.golang.org/protobuf/proto"

	pb "github.com/openrewrite/rewrite-go/parser/proto"
)

func request(files ...[2]string) *bytes.Buffer {
	buf := &bytes.Buffer{}
	for _, f := range files {
		writeReqFrame(buf, []byte(f[0]))
		writeReqFrame(buf, []byte(f[1]))
	}
	return buf
}

func writeReqFrame(buf *bytes.Buffer, b []byte) {
	binary.Write(buf, binary.BigEndian, uint32(len(b)))
	buf.Write(b)
}

type response struct {
	status  byte
	payload []byte
}

func readResponses(t *testing.T, r io.Reader) []response {
	t.Helper()
	var out []response
	for {
		var status [1]byte
		if _, err := io.ReadFull(r, status[:]); err == io.EOF {
			return out
		} else if err != nil {
			t.Fatalf("reading status: %v", err)
		}
		var length uint32
		if err := binary.Read(r, binary.BigEndian, &length); err != nil {
			t.Fatalf("reading length: %v", err)
		}
		payload := make([]byte, length)
		if _, err := io.ReadFull(r, payload); err != nil {
			t.Fatalf("reading payload: %v", err)
		}
		out = append(out, response{status[0], payload})
	}
}

func TestParsesASingleFile(t *testing.T) {
	stdout := &bytes.Buffer{}
	if err := run(request([2]string{"a.go", "package main\n"}), stdout); err != nil {
		t.Fatalf("run failed: %v", err)
	}

	responses := readResponses(t, stdout)
	if len(responses) != 1 {
		t.Fatalf("expected 1 response, got %d", len(responses))
	}
	if responses[0].status != statusOK {
		t.Fatalf("expected ok, got error: %s", responses[0].payload)
	}

	var file pb.GoFile
	if err := proto.Unmarshal(responses[0].payload, &file); err != nil {
		t.Fatalf("unmarshal: %v", err)
	}
	if file.PackageClause.Name.Name != "main" {
		t.Errorf("expected package main, got %q", file.PackageClause.Name.Name)
	}
	if file.SourcePath != "a.go" {
		t.Errorf("expected source path a.go, got %q", file.SourcePath)
	}
}

func TestResponsesMatchRequestOrder(t *testing.T) {
	stdout := &bytes.Buffer{}
	err := run(request(
		[2]string{"a.go", "package alpha\n"},
		[2]string{"b.go", "package bravo\n"},
		[2]string{"c.go", "package charlie\n"},
	), stdout)
	if err != nil {
		t.Fatalf("run failed: %v", err)
	}

	responses := readResponses(t, stdout)
	if len(responses) != 3 {
		t.Fatalf("expected 3 responses, got %d", len(responses))
	}

	for i, want := range []string{"alpha", "bravo", "charlie"} {
		var file pb.GoFile
		if err := proto.Unmarshal(responses[i].payload, &file); err != nil {
			t.Fatalf("unmarshal %d: %v", i, err)
		}
		if file.PackageClause.Name.Name != want {
			t.Errorf("response %d: expected package %q, got %q", i, want, file.PackageClause.Name.Name)
		}
	}
}

// One bad file must not fail the batch, or a single syntax error in a large project
// would take down the whole parse.
func TestASyntaxErrorFailsOnlyItsOwnFile(t *testing.T) {
	stdout := &bytes.Buffer{}
	err := run(request(
		[2]string{"good.go", "package good\n"},
		[2]string{"bad.go", "package !!!\n"},
		[2]string{"alsogood.go", "package fine\n"},
	), stdout)
	if err != nil {
		t.Fatalf("run failed: %v", err)
	}

	responses := readResponses(t, stdout)
	if len(responses) != 3 {
		t.Fatalf("expected 3 responses, got %d", len(responses))
	}
	if responses[0].status != statusOK {
		t.Error("expected the first file to parse")
	}
	if responses[1].status != statusError {
		t.Error("expected the malformed file to report an error")
	}
	if len(responses[1].payload) == 0 {
		t.Error("expected a non-empty error message")
	}
	if responses[2].status != statusOK {
		t.Error("expected parsing to continue after the malformed file")
	}
}

func TestEmptyInputProducesNoResponses(t *testing.T) {
	stdout := &bytes.Buffer{}
	if err := run(&bytes.Buffer{}, stdout); err != nil {
		t.Fatalf("run failed: %v", err)
	}
	if stdout.Len() != 0 {
		t.Errorf("expected no output, got %d bytes", stdout.Len())
	}
}

func TestOversizedFrameIsRejected(t *testing.T) {
	buf := &bytes.Buffer{}
	binary.Write(buf, binary.BigEndian, uint32(maxPayload+1))
	if err := run(buf, &bytes.Buffer{}); err == nil {
		t.Fatal("expected an error for an oversized frame")
	}
}
