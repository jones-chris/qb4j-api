package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import net.querybuilder4j.exceptions.UncleanSqlException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.util.Utils;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.querybuilder4j.sql.statement.criterion.Operator.isNotNull;
import static net.querybuilder4j.sql.statement.criterion.Operator.isNull;

/**
 * This class contains functions that validate a {@link SelectStatement} before it is built.
 */
public class SqlValidator {

    // These characters are the only characters that should be escaped because they can be expected to be in query criteria.
    //  Ex:  SELECT * FROM restaurants WHERE name = 'Tiffany''s';
    private static final Character[] charsNeedingEscaping = new Character[] {'\''};

    // SQL arithmetic, bitwise, comparison, and compound operators per https://www.w3schools.com/sql/sql_operators.asp
    // If any of these strings are contained in SelectStatement, then return false.
    private static final String[] reservedOperators = new String[] {"+", "-", "*", "/", "&", "|", "^", "=", ">", "<", "!=",
            "<>", ">=", "<=", "+=", "-=", "*=", "/=", "%=", "&=", "^-=", "|*="
    };

    // If any of these characters are contained in SelectStatement, then return false.
    private static final String[] forbiddenMarks = new String[] {";", "`", "\""};

    // Ansi keywords per https://docs.snowflake.net/manuals/sql-reference/reserved-keywords.html.  I did not include 'TRUE' and
    //   'FALSE' from the link's list because those are valid SelectStatement filters.
    // If any of these strings are contained in SelectStatement, then return false.
    // Did not include "AND", because this is a common word in filters.
    private static final String[] ansiKeywords = new String[] {"ALL", "ALTER", "ANY", "AS", "ASC", "BETWEEN",
            "BY", "CASE", "CAST", "CHECK", "CLUSTER", "COLUMN", "CONNECT", "CREATE", "CROSS", "CURRENT_DATE",
            "CURRENT_ROLE", "CURRENT_USER", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DELETE", "DESC", "DISTINCT",
            "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FOR", "FROM", "FULL", "GRANT", "GROUP",
            "HAVING", "IDENTIFIED", "ILIKE", "IMMEDIATE", "IN", "INCREMENT", "INNER", "INSERT", "INTERSECT",
            "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS", "MODIFY",
            "NATURAL", "NOT", "NULL", "OF", "ON", "OPTION", "OR", "REGEXP", "RENAME", "REVOKE", "RIGHT",
            "RLIKE", "ROW", "ROWS", "SAMPLE", "SELECT", "SET", "SOME", "START", "TABLE", "TABLESAMPLE",
            "THEN", "TO", "TRIGGER", "UNION", "UNIQUE", "UPDATE", "USING", "VALUES", "VIEW", "WHEN",
            "WHENEVER", "WHERE", "WITH"
    };

    public static void assertIsValid(SelectStatement selectStatement, DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        // Validate columns.
        if (selectStatement.getColumns() == null) {
            throw new IllegalArgumentException("Columns cannot be null");
        }

        if (selectStatement.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Columns is empty");
        }

        assertColumnsExist(selectStatement, databaseMetadataCacheDao);

        // Validate table.
        if (selectStatement.getTable() == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }

        // Validate joins.
        if (selectStatement.getJoins() == null) {
            throw new IllegalArgumentException("Joins cannot be null");
        }

        // Validate criteria.
        if (selectStatement.getCriteria() == null) {
            throw new IllegalArgumentException("The Criteria cannot be null");
        }

        for (Criterion criterion : selectStatement.getCriteria()) {
            assertIsValid(criterion);
        }

        for (CommonTableExpression commonTableExpression : selectStatement.getCommonTableExpressions()) {
            assertIsValid(commonTableExpression);
        }

        // todo:  test if Select Statement joins pass validation.
    }

    private static void assertIsValid(Criterion criterion) {
        // Column and operator must always be non-null.
        if (criterion.getColumn() == null || criterion.getOperator() == null) {
            throw new IllegalStateException("Criterion column and operator are null");
        }

        // If operator is not `isNotNull` and `isNull` and filter is null or an empty string, then criterion is not valid.
        // In other words, the criterion has an operator that expects a non-null or non-empty filter, but the filter is
        // null or an empty string.
        if (! criterion.getOperator().equals(isNotNull) && ! criterion.getOperator().equals(isNull)) {
            if (criterion.getFilter().getValues().isEmpty() || criterion.getFilter().getValues().contains("")) {
                if (criterion.getFilter().getSubQueries().isEmpty()) {
                    throw new IllegalStateException("Criterion filter values are empty or contain an empty string but the operator " +
                            "is not isNull or isNotNull and there are no sub queries");
                }
            }
        }

        // If the criterion's column's data type is a non-string type, check that the values can be converted to the data type.
        int jdbcDataType = criterion.getColumn().getDataType();
        if (jdbcDataType == Types.BIGINT || jdbcDataType == Types.DECIMAL || jdbcDataType == Types.DOUBLE ||
                jdbcDataType == Types.FLOAT || jdbcDataType == Types.INTEGER || jdbcDataType == Types.NUMERIC ||
                jdbcDataType == Types.SMALLINT || jdbcDataType == Types.TINYINT) {
            for (String value : criterion.getFilter().getValues()) {
                try {
                    BigDecimal.valueOf(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    throw new CriterionColumnDataTypeAndFilterMismatchException(
                            Utils.getJdbcSqlType(jdbcDataType),
                            value
                    );
                }
            }
        }
        else if (jdbcDataType == Types.BOOLEAN) {
            for (String value : criterion.getFilter().getValues()) {
                try {
                    Boolean.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new CriterionColumnDataTypeAndFilterMismatchException("BOOLEAN", value);
                }
            }
        }

        // Now check that the criterion's filter does not contain SQL injection attempts.
        assertSqlIsClean(criterion);
    }

    private static void assertIsValid(CommonTableExpression commonTableExpression) {
        assertSqlIsClean(commonTableExpression.getName());
    }

    /**
     * Assert the columns in the {@param selectStatement} exist in the {@param DatabaseMetadataCache}.
     *
     * @param selectStatement {@link SelectStatement}
     * @param databaseMetadataCacheDao {@link DatabaseMetadataCacheDao}
     */
    private static void assertColumnsExist(SelectStatement selectStatement, DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        List<Column> criteriaColumns = selectStatement.getCriteria().stream()
                .map(Criterion::getColumn)
                .collect(Collectors.toList());

        List<Column> selectColumns = selectStatement.getColumns();

        List<Column> allColumns = new ArrayList<>(criteriaColumns);
        allColumns.addAll(selectColumns);

        boolean columnsExist = databaseMetadataCacheDao.columnsExist(allColumns);
        if (! columnsExist) {
            throw new IllegalStateException("A column in the Select Statement's criteria or select clause does not exist");
        }
    }

    public static String escape(String sql) {
        for (Character c : charsNeedingEscaping) {
            sql = sql.replaceAll(c.toString(), c.toString() + c.toString());
        }

        return sql;

    }

    public static void assertSqlIsClean(String str) {
        String upperCaseStr = str.toUpperCase();
        for (String opr : reservedOperators) {
            if (upperCaseStr.contains(opr)) {
                throw new UncleanSqlException();
            }
        }

        for (String mark : forbiddenMarks) {
            if (upperCaseStr.contains(mark)) {
                throw new UncleanSqlException();
            }
        }

        // "\\b%s\\b" worked
        for (String keyword : ansiKeywords) {
            Pattern pattern = Pattern.compile(String.format("(^|\\W)\\Q%s\\E\\W", keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(upperCaseStr);
            if (matcher.find()) {
                throw new UncleanSqlException();
            }
        }

    }

    public static void assertSqlIsClean(Criterion criterion) throws IllegalArgumentException {
        // Only perform this validation on the criterion's filter's values.  Therefore, we assume the criterion's filter's
        // sub queries and parameters have been interpolated into the criterion's filter's values already.
        List<String> values = criterion.getFilter().getValues();
        if (! values.isEmpty()) {
            for (String value : values) {
                assertSqlIsClean(value);
            }
        }
    }

}
