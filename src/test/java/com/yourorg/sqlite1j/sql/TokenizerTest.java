package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenizerTest {
    @Test
    void tokenizesBasicSelect() {
        List<Token> tokens = new Tokenizer().tokenize("SELECT a, b FROM t WHERE a = 1;");
        assertEquals(TokenType.KEYWORD, tokens.get(0).type());
        assertEquals("SELECT", tokens.get(0).lexeme());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals("a", tokens.get(1).lexeme());
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).type());
    }

    @Test
    void tokenizesStringAndNumber() {
        List<Token> tokens = new Tokenizer().tokenize("INSERT INTO t VALUES ('abc', 12.5);");
        assertEquals(TokenType.STRING, tokens.get(5).type());
        assertEquals("abc", tokens.get(5).lexeme());
        assertEquals(TokenType.NUMBER, tokens.get(7).type());
        assertEquals("12.5", tokens.get(7).lexeme());
    }


    @Test
    void tokenizesAllSupportedSymbols() {
        String sql = ", ; ( ) * = < > <= >= != !";
        List<Token> tokens = new Tokenizer().tokenize(sql);
        String[] expected = {",", ";", "(", ")", "*", "=", "<", ">", "<=", ">=", "!=", "!"};

        for (int i = 0; i < expected.length; i++) {
            assertEquals(TokenType.SYMBOL, tokens.get(i).type());
            assertEquals(expected[i], tokens.get(i).lexeme());
        }
        assertEquals(TokenType.EOF, tokens.get(expected.length).type());
    }


    @Test
    void failsOnUnterminatedString() {
        assertThrows(IllegalArgumentException.class,
                () -> new Tokenizer().tokenize("SELECT 'abc"));
    }
}

