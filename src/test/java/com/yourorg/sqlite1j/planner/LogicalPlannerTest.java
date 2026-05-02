package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.UpdateStatement;
import com.yourorg.sqlite1j.sql.DeleteStatement;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalPlannerTest {
    @Test
    void plansSelectWithFilterAndProject() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id", "name")));

        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users WHERE name = 'alice' ORDER BY id DESC LIMIT 5;");
        BoundSelect bound = new NameBinder().bindSelect(stmt, catalog);

        LogicalPlanNode plan = new LogicalPlanner().planSelect(bound);
        assertEquals("Limit", plan.nodeType());

        LimitNode limit = (LimitNode) plan;
        assertEquals(5, limit.limit());
        assertTrue(limit.input() instanceof SortNode);

        SortNode sort = (SortNode) limit.input();
        assertEquals(1, sort.terms().size());
        assertEquals("id", sort.terms().get(0).column());
        assertEquals(false, sort.terms().get(0).ascending());
        assertTrue(sort.input() instanceof ProjectNode);

        ProjectNode project = (ProjectNode) sort.input();
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

    @Test
    void plansUpdateAndDeleteNodes() {
        Parser parser = new Parser();
        UpdateStatement update = parser.parseUpdate("UPDATE users SET name = 'alice' WHERE id = 1;");
        DeleteStatement delete = parser.parseDelete("DELETE FROM users WHERE id = 1;");

        LogicalPlanNode updatePlan = new LogicalPlanner().planUpdate(update);
        LogicalPlanNode deletePlan = new LogicalPlanner().planDelete(delete);

        assertEquals("Update", updatePlan.nodeType());
        assertEquals("Delete", deletePlan.nodeType());
    }

    @Test
    void plansAggregateNodeForAggregateSelect() {
        SchemaCatalog catalog = new SchemaCatalog();
        catalog.register(new TableSchema("users", Set.of("id")));
        SelectStatement stmt = new Parser().parseSelect("SELECT COUNT(*) FROM users;");
        BoundSelect bound = new NameBinder().bindSelect(stmt, catalog);

        LogicalPlanNode plan = new LogicalPlanner().planSelect(bound);
        assertEquals("Aggregate", plan.nodeType());
    }
}
