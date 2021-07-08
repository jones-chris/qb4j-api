package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class OracleSqlBuilderTest extends SqlBuilderCommonTests {

    public OracleSqlBuilderTest() {
        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.sqlBuilder = Mockito.spy(
                new OracleSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService
                )
        );
        this.beginningDelimiter = '"';
        this.endingDelimiter = '"';
    }

    @Test
    public void createLimitClause_noLimitGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createLimitClause_noCriteriaGeneratesStringWithWhereClause() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long limit = 10L;
        selectStatement.setLimit(limit);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertEquals(
                String.format(" WHERE ROWNUM < %s", limit),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

    @Test
    public void createLimitClause_criteriaGeneratesStringWithWhereClause() {
        Column column = new Column("database", "schema", "table", "column", 4, "alias");
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(column)
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long limit = 10L;
        selectStatement.setLimit(limit);
        Filter filter = new Filter(
                List.of("1"), List.of(), List.of()
        );
        selectStatement.setCriteria(
                List.of(
                        new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of())
                )
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertEquals(
                String.format(" AND ROWNUM < %s", limit),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

    @Test
    public void createOffsetClause_noOffsetGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOffsetClause_nonNullOffsetGeneratesNonEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long offset = 10L;
        selectStatement.setOffset(offset);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        assertEquals(
                String.format(" OFFSET %s ROWS ", offset),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

}