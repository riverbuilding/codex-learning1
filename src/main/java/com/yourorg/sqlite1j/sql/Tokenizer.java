package com.yourorg.sqlite1j.sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Tokenizer {
    private static final Set<String> KEYWORDS = new HashSet<>();

    static {
        KEYWORDS.add("SELECT");
        KEYWORDS.add("FROM");
        KEYWORDS.add("WHERE");
        KEYWORDS.add("ORDER");
        KEYWORDS.add("BY");
        KEYWORDS.add("LIMIT");
        KEYWORDS.add("ASC");
        KEYWORDS.add("DESC");
        KEYWORDS.add("INSERT");
        KEYWORDS.add("INTO");
        KEYWORDS.add("VALUES");
        KEYWORDS.add("CREATE");
        KEYWORDS.add("TABLE");
        KEYWORDS.add("UPDATE");
        KEYWORDS.add("SET");
        KEYWORDS.add("DELETE");
        KEYWORDS.add("BEGIN");
        KEYWORDS.add("COMMIT");
        KEYWORDS.add("ROLLBACK");
        KEYWORDS.add("AS");
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            String symbol = readSymbol(input, i);
            if (symbol != null) {
                tokens.add(new Token(TokenType.SYMBOL, symbol, i));
                i += symbol.length();
                continue;
            }

            if (c == '\'') {
                int start = i;
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < input.length() && input.charAt(i) != '\'') {
                    sb.append(input.charAt(i));
                    i++;
                }
                if (i >= input.length()) {
                    throw new IllegalArgumentException("Unterminated string at position " + start);
                }
                i++;
                tokens.add(new Token(TokenType.STRING, sb.toString(), start));
                continue;
            }

            if (Character.isDigit(c)) {
                int start = i;
                i = scanNumber(input, i);
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, i), start));
                continue;
            }

            if (isIdentifierStart(c)) {
                int start = i;
                i = scanIdentifier(input, i);
                String raw = input.substring(start, i);
                String upper = raw.toUpperCase();
                TokenType type = KEYWORDS.contains(upper) ? TokenType.KEYWORD : TokenType.IDENTIFIER;
                tokens.add(new Token(type, raw, start));
                continue;
            }

            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + i);
        }

        tokens.add(new Token(TokenType.EOF, "", input.length()));
        return tokens;
    }

    private static int scanNumber(String input, int i) {
        int index = i;
        boolean seenDot = false;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isDigit(c)) {
                index++;
                continue;
            }
            if (c == '.' && !seenDot) {
                seenDot = true;
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    private static int scanIdentifier(String input, int i) {
        int index = i;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isLetterOrDigit(c) || c == '_') {
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static String readSymbol(String input, int i) {
        char c = input.charAt(i);

        if (c == '<' || c == '>' || c == '!') {
            if (i + 1 < input.length() && input.charAt(i + 1) == '=') {
                return "" + c + '=';
            }
            if (c == '!' ) {
                return "!";
            }
        }

        if (c == ',' || c == ';' || c == '(' || c == ')' || c == '*' || c == '=' || c == '<' || c == '>') {
            return String.valueOf(c);
        }
        return null;
    }
}
