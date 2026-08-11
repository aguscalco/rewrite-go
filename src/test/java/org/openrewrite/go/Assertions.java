package org.openrewrite.go;

import org.openrewrite.SourceFile;
import org.openrewrite.test.SourceSpecs;

import java.util.function.Consumer;

public class Assertions {
    
    public static SourceSpecs go(String before) {
        return go(before, s -> {});
    }
    
    public static SourceSpecs go(String before, String after) {
        return go(before, after, s -> {});
    }
    
    public static SourceSpecs go(String before, Consumer<SourceSpecs> spec) {
        return new GoSourceSpec(before, null, spec);
    }
    
    public static SourceSpecs go(String before, String after, Consumer<SourceSpecs> spec) {
        return new GoSourceSpec(before, after, spec);
    }
    
    private static class GoSourceSpec implements SourceSpecs {
        private final String before;
        private final String after;
        private final Consumer<SourceSpecs> spec;
        
        GoSourceSpec(String before, String after, Consumer<SourceSpecs> spec) {
            this.before = before;
            this.after = after;
            this.spec = spec;
        }
        
        @Override
        public String getBefore() {
            return before;
        }
        
        @Override
        public String getAfter() {
            return after;
        }
        
        @Override
        public void accept(Consumer<SourceSpecs> consumer) {
            spec.accept(consumer);
        }
    }
}
