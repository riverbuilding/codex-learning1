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
            projections.add(parseProjection(cursor));
            while (cursor.matchSymbol(",")) {
                projections.add(parseProjection(cursor));
            }
        }

        cursor.expectKeyword("FROM");
        SelectStatement.FromItem from = parseFromItem(cursor);
        List<SelectStatement.JoinClause> joins = new ArrayList<>();
        while (cursor.matchKeyword("INNER") || cursor.matchKeyword("JOIN")) {
            if ("INNER".equalsIgnoreCase(cursor.previousLexeme())) {
                cursor.expectKeyword("JOIN");
            }
            SelectStatement.FromItem right = parseFromItem(cursor);
            cursor.expectKeyword("ON");
            String leftCol = parseProjection(cursor);
            cursor.expectSymbol("=");
            String rightCol = parseProjection(cursor);
            joins.add(new SelectStatement.JoinClause(right, leftCol, rightCol));
        }

        WhereClause where = null;
        if (cursor.matchKeyword("WHERE")) {
            String column = parseProjection(cursor);
            String operator = cursor.expectComparisonOperator();
            String literal = cursor.expectLiteral();
            where = new WhereClause(column, operator, literal);
        }

        List<SelectStatement.OrderByTerm> orderBy = new ArrayList<>();
        if (cursor.matchKeyword("ORDER")) {
            cursor.expectKeyword("BY");
            orderBy.add(parseOrderByTerm(cursor));
            while (cursor.matchSymbol(",")) {
                orderBy.add(parseOrderByTerm(cursor));
            }
        }

        Integer limit = null;
        if (cursor.matchKeyword("LIMIT")) {
            String literal = cursor.expectLiteral();
            try {
                limit = Integer.parseInt(literal);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Expected numeric LIMIT value");
            }
            if (limit < 0) {
                throw new IllegalArgumentException("Expected non-negative LIMIT value");
            }
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new SelectStatement(projections, from, joins, where, orderBy, limit);
    }

    private SelectStatement.FromItem parseFromItem(Cursor cursor) {
        if (cursor.matchSymbol("(")) {
            String sub = cursor.collectBalancedSelectSql();
            SelectStatement nested = parseSelect(sub);
            String alias = cursor.expectIdentifier();
            return SelectStatement.FromItem.subquery(nested, alias);
        }
        String table = cursor.expectIdentifier();
        String alias = null;
        if (cursor.matchKeyword("AS")) {
            alias = cursor.expectIdentifier();
        } else if (cursor.isIdentifier()) {
            alias = cursor.expectIdentifier();
        }
        return SelectStatement.FromItem.table(table, alias);
    }

    private SelectStatement.OrderByTerm parseOrderByTerm(Cursor cursor) {
        String column = parseProjection(cursor);
        boolean ascending = true;
        if (cursor.matchKeyword("ASC")) {
            ascending = true;
        } else if (cursor.matchKeyword("DESC")) {
            ascending = false;
        }
        return new SelectStatement.OrderByTerm(column, ascending);
    }

    private String parseProjection(Cursor cursor) {
        String name = cursor.expectIdentifierOrKeyword();
        while (cursor.matchSymbol(".")) {
            name = name + "." + cursor.expectIdentifierOrKeyword();
        }
        if (cursor.matchSymbol("(")) {
            String argument;
            if (cursor.matchSymbol("*")) {
                argument = "*";
            } else {
                argument = cursor.expectIdentifier();
            }
            cursor.expectSymbol(")");
            return name.toUpperCase() + "(" + argument + ")";
        }
        return name;
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

    public UpdateStatement parseUpdate(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        cursor.expectKeyword("UPDATE");
        String tableName = cursor.expectIdentifier();
        cursor.expectKeyword("SET");

        List<UpdateStatement.Assignment> assignments = new ArrayList<>();
        assignments.add(parseAssignment(cursor));
        while (cursor.matchSymbol(",")) {
            assignments.add(parseAssignment(cursor));
        }

        WhereClause where = null;
        if (cursor.matchKeyword("WHERE")) {
            String column = parseProjection(cursor);
            String operator = cursor.expectComparisonOperator();
            String literal = cursor.expectLiteral();
            where = new WhereClause(column, operator, literal);
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new UpdateStatement(tableName, assignments, where);
    }

    public DeleteStatement parseDelete(String sql) {
        List<Token> tokens = new Tokenizer().tokenize(sql);
        Cursor cursor = new Cursor(tokens);

        cursor.expectKeyword("DELETE");
        cursor.expectKeyword("FROM");
        String tableName = cursor.expectIdentifier();

        WhereClause where = null;
        if (cursor.matchKeyword("WHERE")) {
            String column = parseProjection(cursor);
            String operator = cursor.expectComparisonOperator();
            String literal = cursor.expectLiteral();
            where = new WhereClause(column, operator, literal);
        }

        cursor.matchSymbol(";");
        cursor.expectEof();
        return new DeleteStatement(tableName, where);
    }

    private UpdateStatement.Assignment parseAssignment(Cursor cursor) {
        String column = parseProjection(cursor);
        cursor.expectSymbol("=");
        String literal = cursor.expectLiteral();
        return new UpdateStatement.Assignment(column, literal);
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
                previousLexeme = token.lexeme();
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
            previousLexeme = token.lexeme();
            index++;
        }

        private String previousLexeme = "";

        private boolean matchKeyword(String keyword) {
            Token token = current();
            if (token.type() == TokenType.KEYWORD && token.lexeme().equalsIgnoreCase(keyword)) {
                previousLexeme = token.lexeme();
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
            if (token.type() == TokenType.SYMBOL) {
                String op = token.lexeme();
                if ("=".equals(op) || "<".equals(op) || ">".equals(op)
                        || "!=".equals(op) || "<=".equals(op) || ">=".equals(op)) {
                    index++;
                    return op;
                }
            }
            throw fail("Expected comparison operator (=, <, >, !=, <=, >=)");
        }

        private void expectEof() {
            Token token = current();
            if (token.type() != TokenType.EOF) {
                throw fail("Expected end of statement");
            }
        }


        private boolean isIdentifier() {
            return current().type() == TokenType.IDENTIFIER;
        }

        private String previousLexeme() {
            return previousLexeme;
        }

        private String collectBalancedSelectSql() {
            int start = index;
            int depth = 1;
            while (index < tokens.size()) {
                Token t = current();
                if (t.type() == TokenType.SYMBOL) {
                    if ("(".equals(t.lexeme())) depth++;
                    if (")".equals(t.lexeme())) {
                        depth--;
                        if (depth == 0) {
                            int end = index;
                            index++;
                            StringBuilder sb = new StringBuilder();
                            for (int i = start; i < end; i++) {
                                if (sb.length() > 0) sb.append(' ');
                                sb.append(tokens.get(i).lexeme());
                            }
                            return sb.toString();
                        }
                    }
                }
                index++;
            }
            throw fail("Unterminated subquery");
        }
        private IllegalArgumentException fail(String message) {
            return new IllegalArgumentException(message + " at token index " + index + " (" + current().lexeme() + ")");
        }
    }
}
