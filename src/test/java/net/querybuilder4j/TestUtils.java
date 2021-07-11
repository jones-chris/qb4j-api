package net.querybuilder4j;

import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.table.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestUtils {

    public static Column buildColumn(int dataType) {
        return new Column("database", "schema", "table", "column", dataType, "alias");
    }

    public static Criterion buildCriterion(Column column, Filter filter) {
        return new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, List.of());
    }

    public static List<CommonTableExpression> buildCommonTableExpressions(List<String> names) {
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

    public static Map<String, List<String>> buildRuntimeArguments(List<String> parameters) {
        return parameters.stream()
                .map(parameter -> parameter + "_arg")
                .collect(
                        Collectors.groupingBy(
                                s -> s.substring(0, s.indexOf("_arg"))
                        )
                );
    }

    public static SelectStatement buildSelectStatement() {
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

}
