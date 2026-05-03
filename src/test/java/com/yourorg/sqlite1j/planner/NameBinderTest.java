package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameBinderTest {
    @Test
    void bindsKnownTableAndColumns() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id", "name")));

        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users WHERE name = 'alice';");
        BoundSelect bound = new NameBinder().bindSelect(stmt, catalog);

        assertEquals("users", bound.table().name());
        assertEquals("id", bound.statement().projections().get(0));
    }

    @Test
    void failsOnUnknownTable() {
        SchemaCatalog catalog = new SchemaCatalog();
        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM missing;");

        assertThrows(IllegalArgumentException.class,
                () -> new NameBinder().bindSelect(stmt, catalog));
    }

    @Test
    void failsOnUnknownProjectionColumn() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id")));
        SelectStatement stmt = new Parser().parseSelect("SELECT name FROM users;");

        assertThrows(IllegalArgumentException.class,
                () -> new NameBinder().bindSelect(stmt, catalog));
    }

    @Test
    void failsOnUnknownWhereColumn() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id")));
        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users WHERE name = 'x';");

        assertThrows(IllegalArgumentException.class,
                () -> new NameBinder().bindSelect(stmt, catalog));
    }

    @Test
    void failsOnUnknownOrderByColumn() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id")));
        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users ORDER BY name;");

        assertThrows(IllegalArgumentException.class,
                () -> new NameBinder().bindSelect(stmt, catalog));
    }

    @Test
    void failsOnAggregateAndNonAggregateMixWithoutGrouping() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id")));
        SelectStatement stmt = new Parser().parseSelect("SELECT COUNT(*), id FROM users;");
        assertThrows(IllegalArgumentException.class,
                () -> new NameBinder().bindSelect(stmt, catalog));
    }
}
