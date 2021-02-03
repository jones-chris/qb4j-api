package net.querybuilder4j.sql.builder;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.querybuilder4j.TestUtils.*;
import static org.junit.Assert.*;


public class SqlPrimerTest {

    @Test
    public void interpolateCriteriaParameters_noParametersMissingAndCommonTableExpressionsAreAllBuilt() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, parameters);
        SelectStatement selectStatement = buildSelectStatement(subQueries, parameters, filter);

        SqlPrimer.interpolateCriteriaParameters(selectStatement);

        List<String> values = selectStatement.getCriteria().get(0).getFilter().getValues();
        assertEquals(4, values.size());
        assertTrue(values.contains(String.format("(SELECT * FROM %s)", selectStatement.getCommonTableExpressions().get(0).getName())));
        assertTrue(values.contains(String.format("(SELECT * FROM %s)", selectStatement.getCommonTableExpressions().get(1).getName())));
        assertTrue(values.contains(selectStatement.getCriteriaArguments().get("parameterName1")));
        assertTrue(values.contains(selectStatement.getCriteriaArguments().get("parameterName2")));
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateCriteriaParameters_parametersMissingThrowsIllegalArgumentException() {
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter(new ArrayList<>(), List.of(), parameters);
        SelectStatement selectStatement = buildSelectStatement(List.of(), parameters, filter);
        selectStatement.getCriteriaArguments().remove(parameters.get(0)); // Remove the argument for "parameterName1"

        SqlPrimer.interpolateCriteriaParameters(selectStatement);
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateCriteriaParameters_subQueriesAreMissingThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, List.of());
        List<CommonTableExpression> commonTableExpressions = TestUtils.buildCommonTableExpressions(subQueries);
        commonTableExpressions.remove(0); // Remove the first common table expression.
        SelectStatement selectStatement = buildSelectStatement(subQueries, List.of(), filter);
        selectStatement.setCommonTableExpressions(commonTableExpressions);

        SqlPrimer.interpolateCriteriaParameters(selectStatement);
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateCriteriaParameters_subQueryHasNullSqlThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, List.of());
        List<CommonTableExpression> commonTableExpressions = TestUtils.buildCommonTableExpressions(subQueries);
        commonTableExpressions.get(0).setSql(null); // Set the SQL of the first common table expression to null.
        SelectStatement selectStatement = this.buildSelectStatement(subQueries, List.of(), filter);
        selectStatement.setCommonTableExpressions(commonTableExpressions);

        SqlPrimer.interpolateCriteriaParameters(selectStatement);
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateCriteriaParameters_subQueryHasEmptySqlStringThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, List.of());
        List<CommonTableExpression> commonTableExpressions = TestUtils.buildCommonTableExpressions(subQueries);
        commonTableExpressions.get(0).setSql(" "); // Set the SQL of the first common table expression to " ".
        SelectStatement selectStatement = this.buildSelectStatement(subQueries, List.of(), filter);
        selectStatement.setCommonTableExpressions(commonTableExpressions);

        SqlPrimer.interpolateCriteriaParameters(selectStatement);
    }

    private SelectStatement buildSelectStatement(List<String> subQueries, List<String> parameters, Filter filter) {
        SelectStatement selectStatement = new SelectStatement();

        // Common Table Expressions
        List<CommonTableExpression> commonTableExpressions = buildCommonTableExpressions(subQueries);
        selectStatement.setCommonTableExpressions(commonTableExpressions);

        // Columns
        Column column = buildColumn(Types.INTEGER);
        selectStatement.setColumns(List.of(column));

        // Table
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        // Criteria
        Criterion criterion = buildCriterion(column, filter);
        selectStatement.setCriteria(List.of(criterion));

        // Criteria Arguments
        Map<String, String> criteriaArguments = buildRuntimeArguments(parameters);
        selectStatement.setCriteriaArguments(criteriaArguments);

        return selectStatement;
    }

}