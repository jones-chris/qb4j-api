package net.querybuilder4j.model.sql;

import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FilterTest {

    @Test
    public void isEmpty_allPropertiesAreEmptyReturnsFalse() {
        Filter filter = new Filter();

        assertTrue(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAndSubQueriesAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));
        filter.setSubQueries(List.of("world"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAndSubQueriesAndParametersAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));
        filter.setSubQueries(List.of("world"));
        filter.setParameters(List.of("!"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void hasSubQueries_subQueriesAreEmptyReturnsFalse() {
        Filter filter = new Filter();

        assertFalse(filter.hasSubQueries());
    }

    @Test
    public void hasSubQueries_subQueriesAreNotEmptyReturnsTrue() {
        Filter filter = new Filter();
        filter.setSubQueries(List.of("hello"));

        assertTrue(filter.hasSubQueries());
    }

    @Test
    public void toSql_nonEmptyValuesFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello", "world"));

        String actualSql = filter.toSql('`', '`');

        assertEquals("(hello, world)", actualSql);
    }

    @Test
    public void toSql_emptyValuesFormattedCorrectly() {
        Filter filter = new Filter();

        String actualSql = filter.toSql('`', '`');

        assertEquals("", actualSql);
    }

    @Test
    public void interpolate_noParametersMissingAndCommonTableExpressionsAreAllBuilt() {
        List<String> subQueries = List.of("subQueryName1", "subQueryName2");
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter();
        filter.setSubQueries(subQueries);
        filter.setParameters(parameters);
        List<CommonTableExpression> commonTableExpressions = this.buildCommonTableExpressions(subQueries);
        Map<String, String> runtimeArguments = this.buildRuntimeArguments(parameters);

        filter.interpolate(commonTableExpressions, runtimeArguments);

        assertEquals(4, filter.getValues().size());
        assertTrue(filter.getValues().contains(String.format("(SELECT * FROM %s)", commonTableExpressions.get(0).getName())));
        assertTrue(filter.getValues().contains(String.format("(SELECT * FROM %s)", commonTableExpressions.get(1).getName())));
        assertTrue(filter.getValues().contains(runtimeArguments.get("parameterName1")));
        assertTrue(filter.getValues().contains(runtimeArguments.get("parameterName2")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void interpolate_parametersMissingThrowsIllegalArgumentException() {
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter();
        filter.setParameters(parameters);
        Map<String, String> runtimeArguments = this.buildRuntimeArguments(parameters);
        runtimeArguments.remove(parameters.get(0)); // Remove the argument for "parameterName1".

        filter.interpolate(List.of(), runtimeArguments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void interpolate_subQueriesAreMissingThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter();
        filter.setSubQueries(subQueries);
        List<CommonTableExpression> commonTableExpressions = this.buildCommonTableExpressions(subQueries);
        commonTableExpressions.remove(0); // Remove the first common table expression.

        filter.interpolate(commonTableExpressions, Map.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void interpolate_subQueryHasNullSqlThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter();
        filter.setSubQueries(subQueries);
        List<CommonTableExpression> commonTableExpressions = this.buildCommonTableExpressions(subQueries);
        commonTableExpressions.get(0).setSql(null);

        filter.interpolate(commonTableExpressions, Map.of());
    }

    @Test(expected = IllegalArgumentException.class)
    public void interpolate_subQueryHasEmptySqlStringThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter();
        filter.setSubQueries(subQueries);
        List<CommonTableExpression> commonTableExpressions = this.buildCommonTableExpressions(subQueries);
        commonTableExpressions.get(0).setSql(" ");

        filter.interpolate(commonTableExpressions, Map.of());
    }

    private List<CommonTableExpression> buildCommonTableExpressions(List<String> names) {
        List<CommonTableExpression> commonTableExpressions = new ArrayList<>();
        for (String name : names) {
            CommonTableExpression commonTableExpression = new CommonTableExpression();
            commonTableExpression.setName(name);
            commonTableExpression.setQueryName("query_" + name);
            commonTableExpression.setSql("SELECT col1, col2 FROM table_" + name + " ");

            commonTableExpressions.add(commonTableExpression);
        }

        return commonTableExpressions;
    }

    private Map<String, String> buildRuntimeArguments(List<String> parameters) {
        return parameters.stream()
                .collect(
                        Collectors.toMap(
                                s -> s, // The parameter is the key.
                                s -> s + "_arg" // The value is "{key}_arg".
                        )
                );
    }
}