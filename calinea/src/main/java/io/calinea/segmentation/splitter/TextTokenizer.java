package io.calinea.segmentation.splitter;

import java.util.List;

/**
 * A text tokenizer interface that splits input text into individual tokens.
 * 
 * This tokenizer serves as a separator that splits text on whitespace and newline characters,
 * preserving all characters in the tokenization process. Each space and newline character
 * becomes its own token, while consecutive non-separator characters are grouped together.
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li>Always splits on newline ('\n') characters</li>
 *   <li>Preserves all original characters in the output tokens</li>
 *   <li>No characters are removed or modified during tokenization</li>
 *   <li>Separator characters (space, newline) are returned as individual tokens</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * TextTokenizer tokenizer = new TextTokenizer.Default();
 * List&lt;String&gt; tokens = tokenizer.tokenize("Hello World\nHey");
 * // Result: ["Hello", " ", "World", "\n", "Hey"]
 * </pre>
 * 
 * @see TextTokenizer.Default for the default implementation
 */
public interface TextTokenizer {
    /**
     * Splits text into tokens, preserving all characters.
     * 
     * Separators (space, newline) are split into individual tokens.
     * Newlines are always split into separate tokens.
     * No characters are removed or modified, only separated.
     * 
     * Example, the default TextTokenizer split: "Hello World\nHey" -> ["Hello", " ", "World", "\n", "Hey"]
     */
    List<String> tokenize(String text);

    /**
     * Default implementation that splits by space and newline, and after dashes.
     */
    class Default implements TextTokenizer {
        @Override
        public List<String> tokenize(String text) {
            // Tokens include: words, spaces, and newlines
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
                } else if (c == '-') {
                    currentToken.append(c);
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
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
