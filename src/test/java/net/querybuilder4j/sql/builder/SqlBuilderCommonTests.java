package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.table.Table;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SqlBuilderCommonTests {

    public static boolean constructorSetsFieldsCorrectly(
            SqlBuilder sqlBuilder,
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            QueryTemplateService queryTemplateService,
            char beginningDelimiter,
            char endingDelimiter
    ) {
        if (! databaseMetadataCacheDao.equals(sqlBuilder.databaseMetadataCacheDao)) { return false; }
        if (! queryTemplateService.equals(sqlBuilder.queryTemplateService)) { return false; }
        if (beginningDelimiter != sqlBuilder.beginningDelimiter) { return false; }
        return endingDelimiter == sqlBuilder.endingDelimiter;
    }

    public static boolean setStatementSetsFieldsCorrectly(SqlBuilder sqlBuilder) {
        SelectStatement selectStatement = buildSelectStatement();

        sqlBuilder.setStatement(selectStatement);

        return selectStatement.equals(sqlBuilder.selectStatement);
    }

    public static boolean buildSqlCallsAllSqlClauseGenerationMethods(SqlBuilder sqlBuilder) {
        SelectStatement selectStatement = buildSelectStatement();

        sqlBuilder.setStatement(selectStatement)
                .buildSql();

        verify(sqlBuilder, times(1)).createCommonTableExpressionClause();
        verify(sqlBuilder, times(1)).createSelectClause();
        verify(sqlBuilder, times(1)).createFromClause();
        verify(sqlBuilder, times(1)).createJoinClause();
        verify(sqlBuilder, times(1)).createWhereClause();
        verify(sqlBuilder, times(1)).createGroupByClause();
        verify(sqlBuilder, times(1)).createOrderByClause();
        verify(sqlBuilder, times(1)).createLimitClause();
        verify(sqlBuilder, times(1)).createOffsetClause();

        return true;
    }

    public static boolean buildSqlSqlStringContainsBeginningAndEndingDelimitersCharacters(
            SqlBuilder sqlBuilder,
            String expectedSql
    ) {
        SelectStatement selectStatement = buildSelectStatement();

        String sql = sqlBuilder.setStatement(selectStatement)
                .buildSql();

        return expectedSql.equals(sql.trim());
    }

    public static boolean createCommonTableExpressionClause_emptyListResultsInEmptyStringBuilder(
            SqlBuilder sqlBuilder
    ) {
        SelectStatement selectStatement = buildSelectStatement();
        sqlBuilder.setStatement(selectStatement);

        sqlBuilder.createCommonTableExpressionClause();

        return "".equals(sqlBuilder.stringBuilder.toString());
    }

    private static SelectStatement buildSelectStatement() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setColumns(
                List.of(
                        new Column("database", "schema", "table", "column", 4, "alias")
                )
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        return selectStatement;
    }

}
