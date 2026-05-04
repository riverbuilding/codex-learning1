package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorCategory;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedDatabaseTest {
    @Test
    void persistsDataAcrossRestart() throws Exception {
        Path dbFile = Files.createTempFile("sqlite1j-persist", ".db");
        Parser parser = new Parser();

        FileBackedDatabase db1 = new FileBackedDatabase(dbFile);
        db1.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE users (id INT, name TEXT);"));
        db1.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES (1, 'alice');"));

        FileBackedDatabase db2 = new FileBackedDatabase(dbFile);
        List<List<DbValue>> rows = db2.executeStatementNormalized(parser.parseSelect("SELECT name FROM users WHERE id = 1;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void reportsCompatibilityDiagnosticsForCorruptFile() throws Exception {
        Path dbFile = Files.createTempFile("sqlite1j-corrupt", ".db");
        Files.write(dbFile, "not-a-sqlite-file".getBytes());

        DbException ex = assertThrows(DbException.class, () -> new FileBackedDatabase(dbFile));
        assertEquals(ErrorCategory.STORAGE_IO, ex.error().category());
        assertTrue(ex.error().message().contains("corrupt") || ex.error().message().contains("unsupported"));
    }
}
