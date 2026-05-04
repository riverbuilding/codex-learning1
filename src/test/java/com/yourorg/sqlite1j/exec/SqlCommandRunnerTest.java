package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorCategory;
import com.yourorg.sqlite1j.sql.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlCommandRunnerTest {
    @Test
    void validatesOrderByColumnsViaBinderBeforeExecution() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());
        runner.execute("CREATE TABLE t (id INT);");

        DbException error = assertThrows(DbException.class, () -> runner.execute("SELECT id FROM t ORDER BY missing;"));
        assertEquals(ErrorCategory.SCHEMA, error.error().category());
        assertTrue(error.error().message().contains("Unknown column in ORDER BY: missing"));
    }
}
