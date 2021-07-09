package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SqlServerSqlBuilderTest extends SqlBuilderCommonTests{

    public SqlServerSqlBuilderTest() {
        super();

        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.sqlBuilder = Mockito.spy(
                new SqlServerSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService
                )
        );
        this.beginningDelimiter = '[';
        this.endingDelimiter = ']';
    }

    @Test
    public void createLimitClause_noLimitGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createLimitClause_limitGeneratesNonEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );
        long limit = 10L;
        selectStatement.setLimit(limit);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertEquals(
                String.format(" FETCH NEXT %s ROWS ONLY ", limit),
                this.sqlBuilder.stringBuilder.toString()
        );
    }

    @Test
    public void createOffsetClause_nullOffsetGeneratesEmptyString() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
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
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
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