package com.yourorg.sqlite1j.sql;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String lexeme;
    private final int position;

    public Token(TokenType type, String lexeme, int position) {
        this.type = Objects.requireNonNull(type, "type");
        this.lexeme = Objects.requireNonNull(lexeme, "lexeme");
        this.position = position;
    }

    public TokenType type() {
        return type;
    }

    public String lexeme() {
        return lexeme;
    }

    public int position() {
        return position;
    }
}
