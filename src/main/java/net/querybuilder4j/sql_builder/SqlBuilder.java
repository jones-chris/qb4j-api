package net.querybuilder4j.sql_builder;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import net.querybuilder4j.cache.InMemoryDatabaseMetadataCacheImpl;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Join;
import net.querybuilder4j.model.Table;
import net.querybuilder4j.model.select_statement.*;
import net.querybuilder4j.model.select_statement.parser.SubQueryParser;
import net.querybuilder4j.model.select_statement.validator.DatabaseMetadataCacheValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.querybuilder4j.model.Join.JoinType.*;

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
     * The class responsible for parsing sub queries.
     */
    protected SubQueryParser subQueryParser;

    /**
     * The cache of the target data source(s) and query template data source, which is built from the Qb4jConfig.json file.
     */
    protected DatabaseMetadataCache databaseMetadataCache;

    /**
     * The class responsible for validating the various fields in the `selectStatement`.
     */
    protected DatabaseMetadataCacheValidator databaseMetadataCacheValidator;

    public SqlBuilder(DatabaseMetadataCache databaseMetadataCache, DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        this.databaseMetadataCache = databaseMetadataCache;
        this.databaseMetadataCacheValidator = databaseMetadataCacheValidator;
    }

    public SqlBuilder setStatement(SelectStatement selectStatement) throws Exception {
        this.selectStatement = selectStatement;
        this.subQueryParser = new SubQueryParser(this.selectStatement, this);

        // Prepare the SelectStatement.
        this.addExcludingJoinCriteria();
        this.addSuppressNullsCriteria();

        // If subQueries has not been set (if this is the case, it will have a 0 size), then set subQueries.
        // This is done because if this SelectStatement is a subquery, then it will already have subQueries and we
        // don't want to change them.
        if (! this.selectStatement.getSubQueries().isEmpty()) {
            this.interpolateSubQueries();
        }

        this.replaceParameters();

        // Validate selectStatement.
        try {
            this.databaseMetadataCacheValidator.passesBasicValidation(this.selectStatement);
            this.databaseMetadataCacheValidator.passesDatabaseValidation(this.selectStatement);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }

        return this;
    }

    public abstract String buildSql() throws Exception;

    /**
     * Creates the SELECT clause of a SELECT SQL statement.
     */
    protected void createSelectClause() {
        List<Column> columns = this.selectStatement.getColumns();
        boolean isDistinct = this.selectStatement.isDistinct();

        if (! columns.isEmpty()) {
            String startSql = (isDistinct) ? "SELECT DISTINCT " : "SELECT ";

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
            CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                    criteria,
                    this.databaseMetadataCache,
                    this.databaseMetadataCacheValidator
            );

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

    /**
     * Adds isNull criterion to criteria if any of the statement's joins are an 'excluding' join, such as LEFT_JOIN_EXCLUDING,
     * RIGHT_JOIN_EXCLUDING, or FULL_OUTER_JOIN_EXCLUDING.
     */
    private void addExcludingJoinCriteria() {
        this.selectStatement.getJoins().forEach(join -> {
            Join.JoinType joinType = join.getJoinType();
            if (joinType.equals(LEFT_EXCLUDING)) {
                this.addCriterionForExcludingJoin(join.getTargetJoinColumns());
            }
            else if (joinType.equals(RIGHT_EXCLUDING)) {
                this.addCriterionForExcludingJoin(join.getParentJoinColumns());
            }
            else if (joinType.equals(FULL_OUTER_EXCLUDING)) {
                List<Column> allJoinColumns = join.getParentJoinColumns().stream()
                        .collect(Collectors.toCollection(join::getTargetJoinColumns));

                this.addCriterionForExcludingJoin(allJoinColumns);
            }
        });
    }

    private void addCriterionForExcludingJoin(List<Column> columns) {
        // Create parent criterion.
        Column firstColumn = columns.get(0);
        Criterion parentCriterion = new Criterion(0,null, Conjunction.And, firstColumn, Operator.isNull, null, null);

        // Create child criteria, if there is more than one column.
        List<Criterion> childCriteria = new ArrayList<>();
        if (columns.size() > 1) {
            for (int i=1; i<columns.size(); i++) {
                Column column = columns.get(i);
                Criterion childCriterion = new Criterion(0, parentCriterion, Conjunction.Or, column, Operator.isNull, null, null);
                childCriteria.add(childCriterion);
            }

            parentCriterion.setChildCriteria(childCriteria);
        }

        // Add parent criterion to this class' criteria.
        this.selectStatement.getCriteria().add(parentCriterion);
    }

    /**
     * Add a criterion to the SelectStatement for each of the SelectStatement's columns so that a "suppress nulls" clause
     * is included in the SelectStatement's SQL string representation's WHERE clause.
     */
    private void addSuppressNullsCriteria() {
        if (this.selectStatement.isSuppressNulls()) {
            // Create root criteria for first column.
            boolean addAndConjunction = ! this.selectStatement.getCriteria().isEmpty();
            Conjunction conjunction = (addAndConjunction) ? Conjunction.And : Conjunction.Empty;
            Column firstColumn = this.selectStatement.getColumns().get(0);
            Criterion parentCriterion = new Criterion(0, null, conjunction, firstColumn, Operator.isNotNull, null, null);

            // Create list of children criteria, which are all columns except for the first column.
            List<Criterion> childCriteria = new ArrayList<>();
            for (int i=1; i<this.selectStatement.getColumns().size(); i++) {
                Column column = this.selectStatement.getColumns().get(i);
                Criterion childCriterion = new Criterion(0, parentCriterion, Conjunction.Or, column, Operator.isNotNull, null, null);
                childCriteria.add(childCriterion);
            }

            // Add child criteria to parent criterion.
            parentCriterion.setChildCriteria(childCriteria);

            // Add parent criterion to SelectStatement's criteria.
            this.selectStatement.getCriteria().add(parentCriterion);
        }
    }

    /**
     * Replaces sub queries in each criterion.  The criterion's filter should be the subquery id that can be retrieved
     * from this class' subQueryParser's builtSubQueries method.
     */
    private void interpolateSubQueries() {
        for (Criterion criterion : this.selectStatement.getCriteria()) {
            String filter = criterion.getFilter();
            String newFilter = filter;

            if (SubQueryParser.argIsSubQuery(filter)) {
                String subquery = this.subQueryParser.getBuiltSubQueries().get(filter);

                if (subquery == null) {
                    throw new RuntimeException("Could not find subquery with name:  " + filter);
                }

                newFilter = "(" + subquery + ")";
            }

            // Join newFilterItems with a "," and set the criterion's filter to the resulting string.
            criterion.setFilter(newFilter);
        }
    }

    /**
     * Checks that there is an equal number of parameters in the criteria (not the criteriaParameters field) and
     * criteriaArguments.  After doing so, it attempts to replace the parameters in the criteria (again, not the
     * criteriaParameters field) with the relevant value from criteriaArguments.
     *
     * @throws Exception if the parameter cannot be found as a key in criteriaArguments.
     */
    private void replaceParameters() throws Exception {
        // Now that we know there are equal number of parameters and arguments, try replacing the parameters with arguments.
        if (this.selectStatement.getCriteriaArguments().size() != 0) {
            for (Criterion criterion : this.selectStatement.getCriteria()) {

                String filter = criterion.getFilter();
                String[] splitFilters = filter.split(",");
                List<String> resultFilters = new ArrayList<>();

                for (String splitFilter : splitFilters) {
                    if (splitFilter.length() >= 1 && splitFilter.substring(0, 1).equals("@")) {
                        String paramName = splitFilter.substring(1);
                        String paramValue = this.selectStatement.getCriteriaArguments().get(paramName);
                        if (paramValue != null) {
                            resultFilters.add(paramValue);
                        } else {
                            String message = String.format("No criteria parameter was found with the name, %s", paramName);
                            throw new Exception(message);
                        }
                    }
                }

                if (resultFilters.size() != 0) {
                    String joinedResultFilters = String.join(",", resultFilters);
                    criterion.setFilter(joinedResultFilters);
                }
            }
        }
    }

}
