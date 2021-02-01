package net.querybuilder4j.sql.statement.criterion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.querybuilder4j.sql.statement.SqlRepresentation;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.querybuilder4j.sql.statement.criterion.Operator.*;
import static net.querybuilder4j.sql.statement.criterion.Operator.isNotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Filter {

    private List<String> values = new ArrayList<>();

    private List<String> subQueries = new ArrayList<>();

    private List<String> parameters = new ArrayList<>();

    public boolean isEmpty() {
        return this.values.isEmpty() && this.subQueries.isEmpty() && this.parameters.isEmpty();
    }

    public boolean hasSubQueries() {
        return ! this.subQueries.isEmpty();
    }

    public void interpolate(List<CommonTableExpression> commonTableExpressions, Map<String, String> runtimeArguments) {
        // Get the Common Table Expressions that are relevant to this Filter by filtering out the Common Table Expressions
        // who's name does not appear in this Filter's subQueries.
        List<CommonTableExpression> relevantCommonTableExpressions = commonTableExpressions.stream()
                .filter(commonTableExpression -> this.subQueries.contains(commonTableExpression.getName()))
                .collect(Collectors.toList());

        // Check that the we found a Common Table Expression for all subQueries.
        if (this.subQueries.size() != relevantCommonTableExpressions.size()) {
            throw new IllegalArgumentException("Size of subQueries and relevantCommonTableExpressions do not match");
        }

        // Add a value for each Common Table Expression.
        final String sql = "(SELECT * FROM %s)";
        relevantCommonTableExpressions.forEach(commonTableExpression -> {
            if (commonTableExpression.isBuilt()) {
                this.values.add(
                        String.format(sql, commonTableExpression.getName())
                );
            } else {
                throw new IllegalArgumentException("CommonTableExpression is not built yet");
            }
        });

        // Check that we have an argument for each parameter.  If not throw an exception.
        if (! runtimeArguments.keySet().containsAll(this.parameters)) {
            throw new IllegalArgumentException("Not all parameters have runtime arguments for " + this.toString());
        }

        // Get the argument for each parameter and add a value for it.
        this.parameters.forEach(parameter ->
                this.values.add(
                        runtimeArguments.get(parameter)
                )
        );
    }

    public String toSql(Operator operator) {
        StringBuilder sql = new StringBuilder();

        if (this.values.isEmpty()) {
            return "";
        }

        if (operator.equals(isNull) || operator.equals(isNotNull)) {
            return "";
        }
        // todo:  Add this BETWEEN and NOT BETWEEN logic eventually.  Check that this logic is correct!
//        else if (operator.equals(between) || operator.equals(notBetween)) {
//            return sql.append(this.values.get(0)).append(" AND ").append(this.values.get(1))
//                    .toString();
//        }
        else {
            return sql.append("(").append(String.join(", ", this.values)).append(")")
                    .toString();
        }

    }

}
