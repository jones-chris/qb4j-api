package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.CriteriaTreeFlattener;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.join.Join;
import net.querybuilder4j.sql.statement.table.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses a SelectStatement to generate a SELECT SQL string.
 */
public abstract class SqlBuilder {

    /**
     * Contains the SQL string that this class' methods will create.  Each class method that is responsible for creating
     * a SQL clause should append it's SQL string to this field.
     */
    StringBuilder stringBuilder = new StringBuilder();

    /**
     * The character to begin wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char beginningDelimiter;

    /**
     * The character to end wrapping the table and column in a SQL statement.  For example, PostgreSQL uses a double quote
     * to wrap the table and column in a SELECT SQL statement like so:  SELECT "employees"."name" FROM "employees".  MySQL
     * uses back ticks like so:  SELECT `employees`.`name` from `employees`.
     */
    protected char endingDelimiter;

    /**
     * The SelectStatement that encapsulates the data to generate the SELECT SQL string.
     */
    protected SelectStatement selectStatement;

    /**
     * The cache of the target data source(s) and query template data source, which is built from the QbConfig.yaml file.
     */
    protected DatabaseMetadataCacheDao databaseMetadataCacheDao;

    /**
     * The class responsible for getting query templates for Common Table Expressions.
     */
    protected QueryTemplateService queryTemplateService;

    public SqlBuilder(DatabaseMetadataCacheDao databaseMetadataCacheDao, QueryTemplateService queryTemplateService) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.queryTemplateService = queryTemplateService;
    }

    public SqlBuilder setStatement(SelectStatement selectStatement) {
        this.selectStatement = selectStatement;

        // Prepare the SelectStatement.
        SqlPrimer.addExcludingJoinCriteria(this.selectStatement);
        SqlPrimer.addSuppressNullsCriteria(this.selectStatement);

        // Get SelectStatement's Common Table Expressions from the database.
        this.queryTemplateService.getCommonTableExpressionSelectStatement(this.selectStatement.getCommonTableExpressions());

        // Interpolate the runtime arguments into the SelectStatement's criteria before validation.
        SqlPrimer.interpolateRuntimeArguments(this.selectStatement);

        // Validate selectStatement.
        SqlValidator.assertIsValid(this.selectStatement, this.databaseMetadataCacheDao);

        /*
        Interpolate the sub queries into the SelectStatement's criteria after validation so that SQL keywords like
        "SELECT" and "*" do not raise an exception when getting the results of a Common Table Expression.
         */
        SqlPrimer.interpolateSubQueries(selectStatement);

        return this;
    }

    public abstract String buildSql();

    /**
     * Creates the WITH (Common Table Expression) clause of a SELECT SQL statement.
     */
    protected void createCommonTableExpressionClause() {
        if (! this.selectStatement.getCommonTableExpressions().isEmpty()) {
            // Instantiate a SqlBuilderFactory inside this SqlBuilder instance to build the SQL for the Common Table Expressions.
            SqlBuilderFactory sqlBuilderFactory = new SqlBuilderFactory(this.databaseMetadataCacheDao, this.queryTemplateService);

            List<String> commonTableExpressionSqlStrings = new ArrayList<>();
            this.selectStatement.getCommonTableExpressions().forEach(commonTableExpression -> {
                // Build the Common Table Expression SELECT SQL.
                String sql = sqlBuilderFactory.buildSqlBuilder(commonTableExpression.getSelectStatement()).buildSql();
                commonTableExpression.setSql(sql);

                // Get the Common Table Expression's wrapped SQL that wraps/encapsulates the SELECT SQL.
                String commonTableExpressionSql = commonTableExpression.toSql(this.beginningDelimiter, this.endingDelimiter);
                commonTableExpressionSqlStrings.add(commonTableExpressionSql);
            });

            // Create the full WITH/Common Table Expression SQL based on all the Common Table Expressions wrapped SQL.
            this.stringBuilder
                    .append("WITH ")
                    .append(String.join(", ", commonTableExpressionSqlStrings));
        }
    }

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     */
    protected void createSelectClause() {
        List<Column> columns = this.selectStatement.getColumns();
        boolean isDistinct = this.selectStatement.isDistinct();

        if (! columns.isEmpty()) {
            String startSql = (isDistinct) ? " SELECT DISTINCT " : " SELECT ";

            StringBuilder sb = new StringBuilder(startSql);

            // Get each column's SQL String representation.
            List<String> columnsSqlStrings = new ArrayList<>();
            columns.forEach(column -> {
                String columnSql = column.toSql(this.beginningDelimiter, this.endingDelimiter);
                columnsSqlStrings.add(columnSql);
            });

            // Join the column SQL strings with a ", " between each SQL string.
            String joinedColumnsSqlStrings = String.join(", ", columnsSqlStrings);

            sb.append(joinedColumnsSqlStrings);

            this.stringBuilder.append(sb);
        }
    }

    /**
     * Creates the FROM clause of a SELECT SQL statement.
     */
    protected void createFromClause() {
        Table table = this.selectStatement.getTable();

        if (table != null) {
            StringBuilder sb = new StringBuilder(" FROM ");
            String tableSqlString = table.toSql(this.beginningDelimiter, this.endingDelimiter);
            sb.append(tableSqlString);
            this.stringBuilder.append(sb);
        }
    }

    /**
     * Creates the JOIN clause of a SELECT SQL statement.
     */
    protected void createJoinClause() {
        List<Join> joins = this.selectStatement.getJoins();

        if (! joins.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            // Get each join's SQL string representation.
            List<String> joinSqlStrings = new ArrayList<>();
            joins.forEach(join -> {
                String joinSqlString = join.toSql(beginningDelimiter, endingDelimiter);
                joinSqlStrings.add(joinSqlString);
            });

            // Join the join SQL strings with a " " between each SQL string.
            String joinedSqlStrings = String.join(" ", joinSqlStrings);

            sb.append(joinedSqlStrings);

            this.stringBuilder.append(sb);
        }
    }

    /**
     * Creates the WHERE clause of a SELECT SQL statement.
     */
    protected void createWhereClause() {
        List<Criterion> criteria = this.selectStatement.getCriteria();

        if (! criteria.isEmpty()) {
            CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(criteria, this.databaseMetadataCacheDao);

            this.stringBuilder.append(" WHERE ");
            String joinedCriteriaSqlStrings = criteriaTreeFlattener.getSqlStringRepresentation(beginningDelimiter, endingDelimiter);
            this.stringBuilder.append(joinedCriteriaSqlStrings);
        }
    }

    /**
     * Creates the GROUP BY clause of a SELECT SQL statement.
     */
    @SuppressWarnings("DuplicatedCode")
    protected void createGroupByClause() {
        List<Column> columns = this.selectStatement.getColumns();

        if (this.selectStatement.isGroupBy() && ! columns.isEmpty()) {
            StringBuilder sb = new StringBuilder(" GROUP BY ");

            // Get each column's SQL string representation.
            List<String> columnsSqlStrings = new ArrayList<>();
            columns.forEach(column -> {
                String columnSqlString = column.toSqlWithoutAlias(beginningDelimiter, endingDelimiter);
                columnsSqlStrings.add(columnSqlString);
            });

            // Join the column SQL strings with a ", " between each SQL string.
            String joinedColumnSqlStrings = String.join(", ", columnsSqlStrings);

            sb.append(joinedColumnSqlStrings);

            this.stringBuilder.append(sb);
        }
    }

    /**
     * Creates the ORDER BY clause of a SELECT SQL statement.
     */
    @SuppressWarnings("DuplicatedCode")
    protected void createOrderByClause() {
        List<Column> columns = this.selectStatement.getColumns();
        boolean isAscending = this.selectStatement.isAscending();

        if (this.selectStatement.isOrderBy() && ! columns.isEmpty()) {
            StringBuilder sb = new StringBuilder(" ORDER BY ");

            // Get each column's SQL string representation.
            List<String> columnsSqlStrings = new ArrayList<>();
            columns.forEach(column -> {
                String columnSqlString = column.toSqlWithoutAlias(beginningDelimiter, endingDelimiter);
                columnsSqlStrings.add(columnSqlString);
            });

            // Join the column SQL strings with a ", " between each SQL string.
            String joinedColumnSqlStrings = String.join(", ", columnsSqlStrings);

            sb.append(joinedColumnSqlStrings);

            // Append " ASC " or " DESC " depending on the value of the `ascending` parameter.
            if (isAscending) {
                sb.append(" ASC ");
            } else {
                sb.append(" DESC ");
            }

            this.stringBuilder.append(sb);
        }
    }

    /**
     * Creates the LIMIT clause of a SELECT SQL statement.
     */
    protected void createLimitClause() {
        Long limit = this.selectStatement.getLimit();

        if (limit != null) {
            this.stringBuilder.append(String.format(" LIMIT %s ", limit));
        }
    }

    /**
     * Creates the OFFSET clause of a SELECT SQL statement.
     */
    protected void createOffsetClause() {
        Long offset = this.selectStatement.getOffset();

        if (offset != null) {
            this.stringBuilder.append(String.format(" OFFSET %s ", offset));
        }
    }

}
