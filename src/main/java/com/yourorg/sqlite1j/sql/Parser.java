package com.yourorg.sqlite1j.sql;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    public TransactionStatement parseTransactionControl(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        TransactionCommand cmd;
        if (cursor.matchKeyword("BEGIN")) {
            cmd = TransactionCommand.BEGIN;
        } else if (cursor.matchKeyword("COMMIT")) {
            cmd = TransactionCommand.COMMIT;
        } else if (cursor.matchKeyword("ROLLBACK")) {
            cmd = TransactionCommand.ROLLBACK;
        } else {
            throw new IllegalArgumentException("Expected transaction command BEGIN/COMMIT/ROLLBACK");
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new TransactionStatement(cmd);
    }

    public SelectStatement parseSelect(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        cursor.expectKeyword("SELECT");
        List<String> projections = new ArrayList<>();
        if (cursor.matchSymbol("*")) {
            projections.add("*");
        } else {
            projections.add(cursor.expectIdentifier());
            while (cursor.matchSymbol(",")) {
                projections.add(cursor.expectIdentifier());
            }
        }

        cursor.expectKeyword("FROM");
        String fromTable = cursor.expectIdentifier();

        WhereClause where = null;
        if (cursor.matchKeyword("WHERE")) {
            String column = cursor.expectIdentifier();
            String operator = cursor.expectComparisonOperator();
            String literal = cursor.expectLiteral();
            where = new WhereClause(column, operator, literal);
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new SelectStatement(projections, fromTable, where);
    }

    public InsertStatement parseInsert(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        cursor.expectKeyword("INSERT");
        cursor.expectKeyword("INTO");
        String tableName = cursor.expectIdentifier();
        cursor.expectKeyword("VALUES");
        cursor.expectSymbol("(");

        List<String> values = new ArrayList<>();
        while (!cursor.matchSymbol(")")) {
            values.add(cursor.expectLiteral());
            if (cursor.matchSymbol(",")) {
                continue;
            }
            cursor.expectSymbol(")");
            break;
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new InsertStatement(tableName, values);
    }

    public CreateTableStatement parseCreateTable(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        cursor.expectKeyword("CREATE");
        cursor.expectKeyword("TABLE");
        String tableName = cursor.expectIdentifier();
        cursor.expectSymbol("(");

        List<ColumnDef> columns = new ArrayList<>();
        while (!cursor.matchSymbol(")")) {
            String columnName = cursor.expectIdentifier();
            String typeName = cursor.expectIdentifierOrKeyword();
            columns.add(new ColumnDef(columnName, typeName));

            if (cursor.matchSymbol(",")) {
                continue;
            }
            cursor.expectSymbol(")");
            break;
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new CreateTableStatement(tableName, columns);
    }

    private static final class Cursor {
        private final List<Token> tokens;
        private int index;

        private Cursor(List<Token> tokens) {
            this.tokens = tokens;
        }

        private Token current() {
            return tokens.get(index);
        }

        private boolean matchSymbol(String symbol) {
            Token token = current();
            if (token.type() == TokenType.SYMBOL && token.lexeme().equals(symbol)) {
                index++;
                return true;
            }
            return false;
        }

        private void expectSymbol(String symbol) {
            if (!matchSymbol(symbol)) {
                throw fail("Expected symbol '" + symbol + "'");
            }
        }

        private void expectKeyword(String keyword) {
            Token token = current();
            if (token.type() != TokenType.KEYWORD || !token.lexeme().equalsIgnoreCase(keyword)) {
                throw fail("Expected keyword '" + keyword + "'");
            }
            index++;
        }

        private boolean matchKeyword(String keyword) {
            Token token = current();
            if (token.type() == TokenType.KEYWORD && token.lexeme().equalsIgnoreCase(keyword)) {
                index++;
                return true;
            }
            return false;
        }

        private String expectIdentifier() {
            Token token = current();
            if (token.type() != TokenType.IDENTIFIER) {
                throw fail("Expected identifier");
            }
            index++;
            return token.lexeme();
        }

        private String expectIdentifierOrKeyword() {
            Token token = current();
            if (token.type() != TokenType.IDENTIFIER && token.type() != TokenType.KEYWORD) {
                throw fail("Expected identifier or type keyword");
            }
            index++;
            return token.lexeme();
        }

        private String expectLiteral() {
            Token token = current();
            if (token.type() == TokenType.STRING || token.type() == TokenType.NUMBER) {
                index++;
                return token.lexeme();
            }
            throw fail("Expected literal value");
        }

        private String expectComparisonOperator() {
            Token token = current();
            if (token.type() == TokenType.SYMBOL && ("=".equals(token.lexeme()) || "<".equals(token.lexeme()) || ">".equals(token.lexeme()))) {
                index++;
                return token.lexeme();
            }
            throw fail("Expected comparison operator (=, <, >)");
        }

        private void expectEof() {
            Token token = current();
            if (token.type() != TokenType.EOF) {
                throw fail("Expected end of statement");
            }
        }

        private IllegalArgumentException fail(String message) {
            return new IllegalArgumentException(message + " at token index " + index + " (" + current().lexeme() + ")");
        }
    }
}
