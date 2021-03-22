package net.querybuilder4j.sql.builder;

import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.CriteriaTreeFlattener;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.join.Join;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.querybuilder4j.sql.statement.join.Join.JoinType.*;

/**
 * This class contains static functions to prepare/prime a SelectStatement before it is validated and then built by the
 * SqlBuilder.
 */
public class SqlPrimer {

    /**
     * Adds isNull criterion to criteria if any of the statement's joins are an 'excluding' join, such as LEFT_JOIN_EXCLUDING,
     * RIGHT_JOIN_EXCLUDING, or FULL_OUTER_JOIN_EXCLUDING.
     */
    public static void addExcludingJoinCriteria(SelectStatement selectStatement) {
        selectStatement.getJoins().forEach(join -> {
            Join.JoinType joinType = join.getJoinType();
            if (joinType.equals(LEFT_EXCLUDING)) {
                addCriterionForExcludingJoin(selectStatement, join.getTargetJoinColumns());
            }
            else if (joinType.equals(RIGHT_EXCLUDING)) {
                addCriterionForExcludingJoin(selectStatement, join.getParentJoinColumns());
            }
            else if (joinType.equals(FULL_OUTER_EXCLUDING)) {
                List<Column> allJoinColumns = join.getParentJoinColumns().stream()
                        .collect(Collectors.toCollection(join::getTargetJoinColumns));

                addCriterionForExcludingJoin(selectStatement, allJoinColumns);
            }
        });
    }

    /**
     * Add a criterion to the SelectStatement for each of the SelectStatement's columns so that a "suppress nulls" clause
     * is included in the SelectStatement's SQL string representation's WHERE clause.
     */
    public static void addSuppressNullsCriteria(SelectStatement selectStatement) {
        if (selectStatement.isSuppressNulls()) {
            // Create root criteria for first column.
            boolean addAndConjunction = ! selectStatement.getCriteria().isEmpty();
            Conjunction conjunction = (addAndConjunction) ? Conjunction.And : Conjunction.Empty;
            Column firstColumn = selectStatement.getColumns().get(0);
            Criterion parentCriterion = new Criterion(0, null, conjunction, firstColumn, Operator.isNotNull, null, null);

            // Create list of children criteria, which are all columns except for the first column.
            List<Criterion> childCriteria = new ArrayList<>();
            for (int i=1; i<selectStatement.getColumns().size(); i++) {
                Column column = selectStatement.getColumns().get(i);
                Criterion childCriterion = new Criterion(0, parentCriterion, Conjunction.Or, column, Operator.isNotNull, null, null);
                childCriteria.add(childCriterion);
            }

            // Add child criteria to parent criterion.
            parentCriterion.setChildCriteria(childCriteria);

            // Add parent criterion to SelectStatement's criteria.
            selectStatement.getCriteria().add(parentCriterion);
        }
    }

    /**
     * Interpolates the SelectStatement's Criteria with the SelectStatement's Criteria Arguments.
     *
     * @param selectStatement {@link SelectStatement}
     */
    public static void interpolateRuntimeArguments(SelectStatement selectStatement) {
        Map<String, List<String>> runtimeParametersAndArguments = selectStatement.getCriteriaArguments();

        // For each criterion...
        selectStatement.getCriteria().forEach(criterion -> {
            // For each parameter...
            criterion.getFilter().getParameters().forEach(parameter -> {
                // Get the runtime arg...
                Optional.ofNullable(runtimeParametersAndArguments.get(parameter))
                        .ifPresentOrElse(
                                // If present and the runtime arg is sub query, then add the sub query name to sub queries...
                                runtimeArguments -> {
                                    runtimeArguments.forEach(runtimeArgument -> {
                                        if (runtimeArgument.startsWith("$")) {
                                            String subQueryName = runtimeArgument.substring(1);
                                            criterion.getFilter().getSubQueries().add(subQueryName);
                                        } else {
                                            // If present and the runtime arg is not sub query, then add the argument to values...
                                            criterion.getFilter().getValues().add(runtimeArgument);
                                        }
                                    });
                                },
                                // If not present, throw an exception.
                                () -> {
                                    throw new IllegalStateException("Could not find runtime argument for parameter, " + parameter);
                                }
                        );
            });
        });
    }

    /**
     * Interpolates the SelectStatement's Criteria with a "SELECT * FROM" with the relevant Common Table Expressions
     * from the SelectStatement.
     *
     * @param selectStatement {@link SelectStatement}
     */
    public static void interpolateSubQueries(SelectStatement selectStatement) {
        // Flatten the criteria first into a single list first.
        List<Criterion> flattenedCriteria = CriteriaTreeFlattener.flattenCriteria(
                selectStatement.getCriteria(),
                new HashMap<>()
        ).values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());


        // For each criterion...
        flattenedCriteria.forEach(criterion -> {
            // For each sub query...
            final String sql = "SELECT * FROM %s";
            criterion.getFilter().getSubQueries().forEach(subQuery -> {
                // Find the Common Table Expression with the same name as the sub query...
                selectStatement.getCommonTableExpressions().stream()
                        .filter(commonTableExpression -> commonTableExpression.getName().equals(subQuery))
                        .findFirst()
                        .ifPresentOrElse(
                                // If present, create the sub query SQL and add it to the values...
                                commonTableExpression -> {
                                    SqlValidator.assertSqlIsClean(commonTableExpression.getName());

                                    criterion.getFilter().getValues().add(
                                            String.format(sql, commonTableExpression.getName())
                                    );
                                },
                                // If not present, throw an exception.
                                () -> {
                                    throw new IllegalStateException("Could not find Common Table Expression with name, " + subQuery);
                                }
                        );
            });
        });
    }

    private static void addCriterionForExcludingJoin(SelectStatement selectStatement, List<Column> columns) {
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
        selectStatement.getCriteria().add(parentCriterion);
    }

}
