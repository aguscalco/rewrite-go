// Command parser is the bridge between the Java recipe module and the Go parser.
//
// It reads a batch of Go sources from stdin and writes one serialized proto.GoFile per
// source to stdout, in the order received. Nothing is buffered across the whole batch, so
// a large project streams rather than accumulating.
//
// Request framing, repeated until stdin closes:
//
//	uint32be lenPath | path bytes | uint32be lenSrc | source bytes
//
// Response framing, one per request, in order:
//
//	uint8 status | uint32be lenPayload | payload
//
// status 0 -> payload is a serialized proto.GoFile
// status 1 -> payload is a UTF-8 error message for that file alone
//
// A per-file status keeps one unparseable file from failing the batch.
package main

import (
	"bufio"
	"encoding/binary"
	"errors"
	"fmt"
	"io"
	"os"

	"google.golang.org/protobuf/proto"

	"github.com/openrewrite/rewrite-go/parser"
)

const (
	statusOK    byte = 0
	statusError byte = 1

	// maxPayload bounds a single frame so a corrupt length cannot make us allocate
	// unbounded memory.
	maxPayload = 64 << 20 // 64 MiB
)

func main() {
	if err := run(os.Stdin, os.Stdout); err != nil {
		fmt.Fprintf(os.Stderr, "rewrite-go-parser: %v\n", err)
		os.Exit(1)
	}
}

func run(stdin io.Reader, stdout io.Writer) error {
	in := bufio.NewReader(stdin)
	out := bufio.NewWriter(stdout)
	defer out.Flush()

	p := parser.New()

	for {
		path, err := readFrame(in)
		if errors.Is(err, io.EOF) {
			return nil
		}
		if err != nil {
			return fmt.Errorf("reading path: %w", err)
		}

		src, err := readFrame(in)
		if err != nil {
			return fmt.Errorf("reading source for %s: %w", path, err)
		}

		file, parseErr := p.ParseSource(string(path), src)
		if parseErr != nil {
			if err := writeFrame(out, statusError, []byte(parseErr.Error())); err != nil {
				return err
			}
			continue
		}

		encoded, err := proto.Marshal(file)
		if err != nil {
			if err := writeFrame(out, statusError, []byte(err.Error())); err != nil {
				return err
			}
			continue
		}

		if err := writeFrame(out, statusOK, encoded); err != nil {
			return err
		}
		// Flush per file so the caller can consume incrementally instead of waiting
		// for the whole batch.
		if err := out.Flush(); err != nil {
			return err
		}
	}
}

func readFrame(r *bufio.Reader) ([]byte, error) {
	var length uint32
	if err := binary.Read(r, binary.BigEndian, &length); err != nil {
		return nil, err
	}
	if length > maxPayload {
		return nil, fmt.Errorf("frame of %d bytes exceeds the %d byte limit", length, maxPayload)
	}
	buf := make([]byte, length)
	if _, err := io.ReadFull(r, buf); err != nil {
		return nil, err
	}
	return buf, nil
}

func writeFrame(w *bufio.Writer, status byte, payload []byte) error {
	if err := w.WriteByte(status); err != nil {
		return err
	}
	if err := binary.Write(w, binary.BigEndian, uint32(len(payload))); err != nil {
		return err
	}
	_, err := w.Write(payload)
	return err
}
