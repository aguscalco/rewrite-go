package org.openrewrite.go;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.SourceFile;
import org.openrewrite.go.internal.GoDeserializer;
import org.openrewrite.go.proto.GoProto;
import org.openrewrite.go.tree.GoFile;
import org.openrewrite.tree.ParseError;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Parses Go sources by delegating to the {@code rewrite-go-parser} binary built from the
 * {@code parser/} module, and deserializing the protobuf it returns.
 * <p>
 * The whole batch goes to one subprocess rather than one process per file, because process
 * startup dominates for anything but the smallest inputs.
 * <p>
 * Build the binary with:
 * <pre>
 * cd parser &amp;&amp; go build -o rewrite-go-parser ./cmd/parser
 * </pre>
 * and point the parser at it with {@link Builder#parserBinary(Path)}, the
 * {@code rewrite.go.parser} system property, or the {@code REWRITE_GO_PARSER} environment
 * variable. Failing all three, {@code rewrite-go-parser} is looked up on {@code PATH}.
 */
public class GoParser implements Parser {

    private static final byte STATUS_OK = 0;

    private final String parserBinary;

    GoParser(String parserBinary) {
        this.parserBinary = parserBinary;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Stream<SourceFile> parseInputs(Iterable<Input> sources, Path relativeTo, ExecutionContext ctx) {
        List<Input> inputs = new ArrayList<>();
        for (Input input : sources) {
            inputs.add(input);
        }
        if (inputs.isEmpty()) {
            return Stream.empty();
        }

        List<SourceFile> parsed = new ArrayList<>(inputs.size());
        Process process = null;
        try {
            process = new ProcessBuilder(parserBinary).start();

            // Requests are written in full before responses are read. The subprocess flushes
            // each response as it goes, so this only deadlocks if a batch exceeds the pipe
            // buffer in both directions at once; sources are written first and stdin closed,
            // which the subprocess needs in order to finish.
            writeRequests(process, inputs, relativeTo, ctx);
            readResponses(process, inputs, relativeTo, ctx, parsed);

            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("rewrite-go-parser exited with status " + exit + ": " + readStderr(process));
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                "Unable to run the Go parser binary '" + parserBinary + "'. Build it with " +
                "`cd parser && go build -o rewrite-go-parser ./cmd/parser` and put it on PATH, " +
                "or set the rewrite.go.parser system property to its location.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while parsing Go sources", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return parsed.stream();
    }

    private void writeRequests(Process process, List<Input> inputs, Path relativeTo, ExecutionContext ctx)
        throws IOException {
        try (DataOutputStream out = new DataOutputStream(process.getOutputStream())) {
            for (Input input : inputs) {
                byte[] path = input.getRelativePath(relativeTo).toString().getBytes(StandardCharsets.UTF_8);
                byte[] source = readFully(input.getSource(ctx));
                out.writeInt(path.length);
                out.write(path);
                out.writeInt(source.length);
                out.write(source);
            }
        }
    }

    private void readResponses(Process process, List<Input> inputs, Path relativeTo, ExecutionContext ctx,
                               List<SourceFile> parsed) throws IOException {
        GoDeserializer deserializer = new GoDeserializer();
        try (DataInputStream in = new DataInputStream(process.getInputStream())) {
            for (Input input : inputs) {
                int status = in.read();
                if (status < 0) {
                    throw new IOException("rewrite-go-parser produced no response for " + input.getPath() +
                                          ": " + readStderr(process));
                }

                byte[] payload = new byte[in.readInt()];
                in.readFully(payload);

                if (status != STATUS_OK) {
                    String message = new String(payload, StandardCharsets.UTF_8);
                    parsed.add(ParseError.build(this, input, relativeTo, ctx, new IllegalStateException(message)));
                    continue;
                }

                // The subprocess does not know the path the caller wants the file recorded
                // under, so it is applied here rather than trusting what came back.
                GoFile file = deserializer.deserialize(GoProto.GoFile.parseFrom(payload));
                parsed.add(file.withSourcePath(input.getRelativePath(relativeTo)));
            }
        }
    }

    private static byte[] readFully(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static String readStderr(Process process) {
        try {
            return new String(readFully(process.getErrorStream()), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "<stderr unavailable>";
        }
    }

    @Override
    public boolean accept(Path path) {
        return path.toString().endsWith(".go");
    }

    @Override
    public Path sourcePathFromSourceText(Path prefix, String sourceCode) {
        return prefix.resolve("file.go");
    }

    public static class Builder extends Parser.Builder {
        private String parserBinary;

        public Builder() {
            super(GoFile.class);
        }

        /**
         * Explicit location of the parser binary. Takes precedence over the system property,
         * the environment variable, and PATH lookup.
         */
        public Builder parserBinary(Path path) {
            this.parserBinary = path.toString();
            return this;
        }

        @Override
        public GoParser build() {
            return new GoParser(parserBinary != null ? parserBinary : discoverBinary());
        }

        @Override
        public String getDslName() {
            return "go";
        }

        private static String discoverBinary() {
            String property = System.getProperty("rewrite.go.parser");
            if (property != null && !property.isEmpty()) {
                return property;
            }
            String environment = System.getenv("REWRITE_GO_PARSER");
            if (environment != null && !environment.isEmpty()) {
                return environment;
            }
            return "rewrite-go-parser";
        }
    }
}
