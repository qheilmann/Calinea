package io.calinea.segmentation.splitter;

import java.util.List;

public interface TextTokenizer {
    /**
     * Splits the text into tokens (words, separators, and newlines).
     * For example, "Hello World" -> ["Hello", " ", "World"]
     * "Hello\nWorld" -> ["Hello", "\n", "World"]
     */
    List<String> tokenize(String text);

    /**
     * Default implementation that splits by space and newline.
     */
    class Default implements TextTokenizer {
        @Override
        public List<String> tokenize(String text) {
            List<String> tokens = new java.util.ArrayList<>();
            StringBuilder currentToken = new StringBuilder();
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == ' ' || c == '\n') {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString());
                        currentToken.setLength(0);
                    }
                    tokens.add(String.valueOf(c));
                } else {
                    currentToken.append(c);
                }
            }
            if (currentToken.length() > 0) {
                tokens.add(currentToken.toString());
            }
            return tokens;
        }
    }
}
