package net.querybuilder4j.sql.builder;

import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.join.Join;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public abstract class SqlBuilderCommonTests {

    protected SqlBuilder sqlBuilder;

    protected DatabaseMetadataCacheDao databaseMetadataCacheDao;

    protected QueryTemplateService queryTemplateService;

    protected char beginningDelimiter;

    protected char endingDelimiter;

    @Before
    public void beforeEach() {
        when(this.databaseMetadataCacheDao.columnsExist(anyListOf(Column.class)))
                .thenReturn(true);
    }

    @Test
    public void constructor_SetsFieldsCorrectly() {
        assertEquals(this.databaseMetadataCacheDao, this.sqlBuilder.databaseMetadataCacheDao);
        assertEquals(this.queryTemplateService, this.sqlBuilder.queryTemplateService);
        assertEquals(this.beginningDelimiter, this.sqlBuilder.beginningDelimiter);
        assertEquals(this.endingDelimiter, this.sqlBuilder.endingDelimiter);
    }

    @Test
    public void setStatement_setsFieldsCorrectly() {
        SelectStatement selectStatement = this.buildSelectStatement();

        this.sqlBuilder.setStatement(selectStatement);

        assertEquals(selectStatement, this.sqlBuilder.selectStatement);
    }

    @Test
    public void buildSql_callsAllSqlClauseGenerationMethods() {
        SelectStatement selectStatement = this.buildSelectStatement();

        this.sqlBuilder.setStatement(selectStatement)
                .buildSql();

        verify(this.sqlBuilder, times(1)).createCommonTableExpressionClause();
        verify(this.sqlBuilder, times(1)).createSelectClause();
        verify(this.sqlBuilder, times(1)).createFromClause();
        verify(this.sqlBuilder, times(1)).createJoinClause();
        verify(this.sqlBuilder, times(1)).createWhereClause();
        verify(this.sqlBuilder, times(1)).createGroupByClause();
        verify(this.sqlBuilder, times(1)).createOrderByClause();
        verify(this.sqlBuilder, times(1)).createLimitClause();
        verify(this.sqlBuilder, times(1)).createOffsetClause();
    }

    @Test
    public void buildSql_sqlStringContainsBeginningAndEndingDelimitersCharacters() {
        SelectStatement selectStatement = this.buildSelectStatement();

        String sql = this.sqlBuilder.setStatement(selectStatement)
                .buildSql();

        assertEquals(this.getExpectedSql(), sql.trim());
    }

    @Test
    public void createCommonTableExpressionClause_emptyListResultsInEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createCommonTableExpressionClause();

        assertEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createCommonTableExpressionClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName("name");
        commonTableExpression.setQueryName("cte1");
        commonTableExpression.setSelectStatement(this.buildSelectStatement());
        selectStatement.getCommonTableExpressions().add(commonTableExpression);
        this.sqlBuilder.setStatement(selectStatement);
        when(this.databaseMetadataCacheDao.findDatabases("database"))
                .thenReturn(
                        new Database("database", DatabaseType.MySql)
                );

        this.sqlBuilder.createCommonTableExpressionClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createJoinClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        Join join = new Join();
        join.setJoinType(Join.JoinType.LEFT);
        join.setParentJoinColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        join.setParentTable(
                new Table("database", "schema", "table")
        );
        join.setTargetJoinColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        join.setTargetTable(
                new Table("database", "schema", "table")
        );
        selectStatement.getJoins().add(join);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createJoinClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createWhereClause_nonEmptyListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 4, "alias"),
                        Operator.equalTo,
                        new Filter(
                                List.of("1"),
                                List.of(),
                                List.of()
                        ),
                        List.of()
                )
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createWhereClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createGroupByClause_isGroupByAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        selectStatement.setGroupBy(true);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createGroupByClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOrderByClause_ascendingAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(true);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createOrderByClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOrderByClause_descendingAndNonEmptyColumnListResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(false);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createOrderByClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createLimitClause_nonNullLimitResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        long limit = 10L;
        selectStatement.setLimit(limit);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createLimitClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    @Test
    public void createOffsetClause_nonNullOffsetResultsInNonEmptyStringBuilder() {
        SelectStatement selectStatement = this.buildSelectStatement();
        long offset = 10L;
        selectStatement.setOffset(offset);
        this.sqlBuilder.setStatement(selectStatement);

        this.sqlBuilder.createOffsetClause();

        assertNotEquals("", this.sqlBuilder.stringBuilder.toString());
    }

    private SelectStatement buildSelectStatement() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(
                new Database("database", DatabaseType.MySql)
        );
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        return selectStatement;
    }

    private String getExpectedSql() {
        return String.format(
                "SELECT  %sschema%s.%stable%s.%scolumn%s AS alias  FROM  %sschema%s.%stable%s",
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter,
                this.beginningDelimiter, this.endingDelimiter
        );
    }

}
