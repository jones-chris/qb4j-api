package net.querybuilder4j.sql.statement.validator;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.constants.Constants;
import net.querybuilder4j.exceptions.SqlTypeNotRecognizedException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.builder.SqlCleanser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.querybuilder4j.sql.builder.SqlCleanser.sqlIsClean;

@Service
public class DatabaseMetadataCacheValidator {

    private DatabaseMetadataCache databaseMetadataCache;

    @Autowired
    public DatabaseMetadataCacheValidator(DatabaseMetadataCache databaseMetadataCache) {
        this.databaseMetadataCache = databaseMetadataCache;
    }

    /**
     * Test if stmt passes basic validation.
     *
     * @param selectStatement The SelectStatement object to validate.
     */
    public void assertPassesBasicValidation(SelectStatement selectStatement) {
        // Validate columns.
        if (selectStatement.getColumns() == null) {
            throw new IllegalArgumentException("Columns cannot be null");
        }

        if (selectStatement.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Columns is empty");
        }

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
            if (! criterion.isValid()) {
                throw new IllegalArgumentException(String.format("A criterion is not valid:  %s", criterion));
            }
        }

        // todo:  test if stmt joins pass basic validation.
    }

    public void assertPassesDatabaseValidation(SelectStatement selectStatement) {
        // Validate selectStatement's columns and selectStatement's criteria's columns.
        List<Column> criteriaColumns = selectStatement.getCriteria().stream()
                .map(Criterion::getColumn)
                .collect(Collectors.toList());

        List<Column> selectColumns = selectStatement.getColumns();

        List<Column> allColumns = new ArrayList<>(criteriaColumns);
        allColumns.addAll(selectColumns);

        assert this.databaseMetadataCache.columnsExist(allColumns);

        // Validate selectStatement's criteria.
        assertCriteriaAreValid(selectStatement);
    }

    /**
     * Determines if the stmt's criteria are valid.  False is never actually returned - instead an exception will be thrown.
     * True will be returned if the criteria are valid.
     */
    private void assertCriteriaAreValid(SelectStatement selectStatement) {
        for (Criterion criterion : selectStatement.getCriteria()) {
            if (! criterion.isValid()) {
                throw new IllegalArgumentException("This criteria is not valid:  " + criterion);
            }

            if (! sqlIsClean(criterion)) {
                throw new IllegalArgumentException("This criterion failed to be clean SQL:  " + criterion);
            }

            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
            // If so, throw an exception.
            if (! criterion.getFilter().getValues().isEmpty()) {
                int columnDataType = this.databaseMetadataCache.getColumnDataType(criterion.getColumn());
                boolean shouldHaveQuotes = isColumnQuoted(columnDataType);

                if (! shouldHaveQuotes && ! criterion.getFilter().getValues().isEmpty()) {
                    criterion.getFilter().getValues().forEach(value -> {
                        if (! SqlCleanser.canParseNonQuotedFilter(value)) {
                            throw new RuntimeException("The column is a number type, but the criteria's filter is not an number type: " + criterion);
                        }
                    });
                }
            }
        }
    }

    /**
     * First, gets the SQL JDBC Type for the table and column parameters.  Then, gets a boolean from the typeMappings
     * class field associated with the SQL JDBC Types parameter, which is an int.  The typeMappings field will return
     * true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and false if it should not be quoted.
     *
     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
     *
     * @param dataType The data type to inquire as to whether column members with this data type should be wrapped in single
     *                 quotes.
     * @return boolean
     */
    public boolean isColumnQuoted(int dataType) {
        Boolean isQuoted = Constants.TYPE_MAPPINGS.get(dataType); //todo:  make typeMappings a public static field in SelectStatementValidator so that it can be called?  Maybe even put it in Constants class because it's called by SelectStatementValidator and SqlBuilder?

        if (isQuoted == null) {
            throw new SqlTypeNotRecognizedException(dataType);
        }

        return isQuoted;
    }

}
