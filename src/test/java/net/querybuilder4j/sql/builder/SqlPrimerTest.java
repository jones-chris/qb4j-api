package net.querybuilder4j.sql.builder;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.join.Join;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SqlPrimerTest {

    @Test
    public void interpolateRuntimeArguments_noParametersMissingAndCommonTableExpressionsAreAllBuilt() {
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter(new ArrayList<>(), List.of(), parameters);
        SelectStatement selectStatement = buildSelectStatement(List.of(), parameters, filter);

        SqlPrimer.interpolateRuntimeArguments(selectStatement);

        List<String> values = selectStatement.getCriteria().get(0).getFilter().getValues();
        assertEquals(2, values.size());
        assertTrue(values.contains(selectStatement.getCriteriaArguments().get("parameterName1").get(0)));
        assertTrue(values.contains(selectStatement.getCriteriaArguments().get("parameterName2").get(0)));
    }

    @Test
    public void interpolateSubQueries_commonTableExpressionsAreAllBuilt() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, List.of());
        SelectStatement selectStatement = buildSelectStatement(subQueries, List.of(), filter);

        SqlPrimer.interpolateSubQueries(selectStatement);

        List<String> values = selectStatement.getCriteria().get(0).getFilter().getValues();
        assertEquals(2, values.size());
        assertTrue(values.contains(String.format("SELECT * FROM %s", selectStatement.getCommonTableExpressions().get(0).getName())));
        assertTrue(values.contains(String.format("SELECT * FROM %s", selectStatement.getCommonTableExpressions().get(1).getName())));
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateRuntimeArguments_parametersMissingThrowsIllegalArgumentException() {
        List<String> parameters = List.of("parameterName1", "parameterName2");
        Filter filter = new Filter(new ArrayList<>(), List.of(), parameters);
        SelectStatement selectStatement = buildSelectStatement(List.of(), parameters, filter);
        selectStatement.getCriteriaArguments().remove(parameters.get(0)); // Remove the argument for "parameterName1"

        SqlPrimer.interpolateRuntimeArguments(selectStatement);
    }

    @Test(expected = IllegalStateException.class)
    public void interpolateSubQueries_subQueriesAreMissingThrowsIllegalArgumentException() {
        List<String> subQueries = List.of("subQuery1", "subQuery2");
        Filter filter = new Filter(new ArrayList<>(), subQueries, List.of());
        List<CommonTableExpression> commonTableExpressions = TestUtils.buildCommonTableExpressions(subQueries);
        commonTableExpressions.remove(0); // Remove the first common table expression.
        SelectStatement selectStatement = buildSelectStatement(subQueries, List.of(), filter);
        selectStatement.getCommonTableExpressions().clear();
        selectStatement.getCommonTableExpressions().addAll(commonTableExpressions);

        SqlPrimer.interpolateSubQueries(selectStatement);
    }

    @Test
    public void addExcludingJoinCriteria_leftExcludingAddsCriterion() {
        int numberOfJoinColumns = 1;
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getJoins().add(
                this.buildJoin(Join.JoinType.LEFT_EXCLUDING, numberOfJoinColumns)
        );

        SqlPrimer.addExcludingJoinCriteria(selectStatement);

        assertEquals(numberOfJoinColumns, selectStatement.getCriteria().size());
    }

    @Test
    public void addExcludingJoinCriteria_rightExcludingAddsCriterion() {
        int numberOfJoinColumns = 1;
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getJoins().add(
                this.buildJoin(Join.JoinType.RIGHT_EXCLUDING, numberOfJoinColumns)
        );

        SqlPrimer.addExcludingJoinCriteria(selectStatement);

        assertEquals(numberOfJoinColumns, selectStatement.getCriteria().size());
    }

    @Test
    public void addExcludingJoinCriteria_fullOuterExcludingAddsCriterion() {
        int numberOfJoinColumns = 1;
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getJoins().add(
                this.buildJoin(Join.JoinType.FULL_OUTER_EXCLUDING, numberOfJoinColumns)
        );

        SqlPrimer.addExcludingJoinCriteria(selectStatement);

        assertEquals(numberOfJoinColumns, selectStatement.getCriteria().size());
        assertEquals(numberOfJoinColumns, selectStatement.getCriteria().get(0).getChildCriteria().size());
    }

    @Test
    public void addSuppressNullsCriteria_addsCriterion() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().addAll(
                List.of(
                    TestUtils.buildColumn(Types.INTEGER),
                    TestUtils.buildColumn(Types.VARCHAR)
                )
        );
        selectStatement.setSuppressNulls(true);

        SqlPrimer.addSuppressNullsCriteria(selectStatement);

        assertEquals(1, selectStatement.getCriteria().size());
        assertEquals(1, selectStatement.getCriteria().get(0).getChildCriteria().size());
    }

    @Test
    public void interpolateRuntimeArguments_successfullyExtractsSubQueryNameFromCriterionFilter() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setCriteriaArguments(
                Map.of(
                        "param1",
                        List.of("$subQuery1")
                )
        );
        Filter filter = new Filter();
        filter.setParameters(
                List.of("param1")
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        TestUtils.buildColumn(Types.VARCHAR),
                        Operator.in,
                        filter,
                        new ArrayList<>()
                )
        );

        SqlPrimer.interpolateRuntimeArguments(selectStatement);

        assertEquals(1, selectStatement.getCriteria().size());
        assertEquals(1, selectStatement.getCriteria().get(0).getFilter().getSubQueries().size());
        assertEquals("subQuery1", selectStatement.getCriteria().get(0).getFilter().getSubQueries().get(0));
    }

    private SelectStatement buildSelectStatement(List<String> subQueries, List<String> parameters, Filter filter) {
        SelectStatement selectStatement = new SelectStatement();

        // Common Table Expressions
        List<CommonTableExpression> commonTableExpressions = TestUtils.buildCommonTableExpressions(subQueries);
        selectStatement.getCommonTableExpressions().addAll(commonTableExpressions);

        // Columns
        Column column = TestUtils.buildColumn(Types.INTEGER);
        selectStatement.getColumns().add(column);

        // Table
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        // Criteria
        Criterion criterion = TestUtils.buildCriterion(column, filter);
        selectStatement.getCriteria().add(criterion);

        // Criteria Arguments
        Map<String, List<String>> criteriaArguments = TestUtils.buildRuntimeArguments(parameters);
        selectStatement.setCriteriaArguments(criteriaArguments);

        return selectStatement;
    }

    private Join buildJoin(Join.JoinType joinType, int numberOfJoinColumns) {
        Join join = new Join();

        join.setJoinType(joinType);

        join.setTargetTable(
                new Table("database", "schema", "targetTable")
        );

        join.setParentTable(
                new Table("database", "schema", "parentTable")
        );

        for (int i=0; i<numberOfJoinColumns; i++) {
            join.getTargetJoinColumns().add(
                    new Column("database", "schema", "targetTable", "column" + i, 4, "alias")
            );
        }

        for (int i=0; i<numberOfJoinColumns; i++) {
            join.getParentJoinColumns().add(
                    new Column("database", "schema", "parentTable", "column" + i, 4, "alias")
            );
        }

        return join;
    }

}