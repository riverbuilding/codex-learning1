package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalPlannerTest {
    @Test
    void plansSelectWithFilterAndProject() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id", "name")));

        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users WHERE name = 'alice';");
        BoundSelect bound = new NameBinder().bindSelect(stmt, catalog);

        LogicalPlanNode plan = new LogicalPlanner().planSelect(bound);
        assertEquals("Project", plan.nodeType());

        ProjectNode project = (ProjectNode) plan;
        assertEquals(1, project.projections().size());
        assertEquals("id", project.projections().get(0));
        assertTrue(project.input() instanceof FilterNode);

        FilterNode filter = (FilterNode) project.input();
        assertEquals("name", filter.whereClause().column());
        assertTrue(filter.input() instanceof TableScanNode);
    }

    @Test
    void plansInsertNode() {
        InsertStatement stmt = new Parser().parseInsert("INSERT INTO users VALUES ('alice', 1);");
        LogicalPlanNode plan = new LogicalPlanner().planInsert(stmt);

        assertEquals("Insert", plan.nodeType());
        InsertNode insert = (InsertNode) plan;
        assertEquals("users", insert.tableName());
        assertEquals(2, insert.values().size());
    }
}
