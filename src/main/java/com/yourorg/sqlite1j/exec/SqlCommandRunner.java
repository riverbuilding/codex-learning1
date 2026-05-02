package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorParity;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.Statement;

import java.util.List;

import com.yourorg.sqlite1j.types.DbValue;

public final class SqlCommandRunner {
    private final Parser parser;
    private final InMemoryDatabase db;

    public SqlCommandRunner(Parser parser, InMemoryDatabase db) {
        this.parser = parser;
        this.db = db;
    }

    public List<List<DbValue>> execute(String sql) {
        try {
            Statement stmt = parseStatement(sql);
            return db.executeStatementNormalized(stmt);
        } catch (DbException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DbException(ErrorParity.normalizeThrowable(e));
        }
    }

    private Statement parseStatement(String sql) {
        String normalized = sql.trim().toUpperCase();
        if (normalized.startsWith("SELECT")) {
            return parser.parseSelect(sql);
        }
        if (normalized.startsWith("INSERT")) {
            return parser.parseInsert(sql);
        }
        if (normalized.startsWith("CREATE TABLE")) {
            return parser.parseCreateTable(sql);
        }
        if (normalized.startsWith("BEGIN") || normalized.startsWith("COMMIT") || normalized.startsWith("ROLLBACK")) {
            return parser.parseTransactionControl(sql);
        }
        throw new IllegalArgumentException("Expected SQL statement type (CREATE/INSERT/SELECT/BEGIN/COMMIT/ROLLBACK)");
    }
}
