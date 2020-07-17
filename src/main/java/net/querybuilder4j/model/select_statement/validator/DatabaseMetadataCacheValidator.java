package net.querybuilder4j.model.select_statement.validator;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import net.querybuilder4j.constants.Constants;
import net.querybuilder4j.model.select_statement.Criterion;
import net.querybuilder4j.model.select_statement.SelectStatement;
import net.querybuilder4j.sql_builder.SqlCleanser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static net.querybuilder4j.sql_builder.SqlCleanser.sqlIsClean;

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
     * @return boolean
     * @throws IllegalArgumentException If the selectStatement does not pass validation.
     */
    public boolean passesBasicValidation(SelectStatement selectStatement) throws Exception {
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
            throw new Exception("The Criteria cannot be null");
        }

        for (Criterion criterion : selectStatement.getCriteria()) {
            boolean isValid = criterion.isValid();
            if (! isValid) {
                throw new IllegalArgumentException(String.format("A criterion is not valid:  %s", criterion));
            }
        }

        // todo:  test if stmt joins pass basic validation.

        return true;
    }

    public boolean passesDatabaseValidation(SelectStatement selectStatement) throws Exception {
        // Validate selectStatement's columns and selectStatement's criteria's columns.
        boolean columnsExist = selectStatement.getCriteria().stream()
                .map(Criterion::getColumn)
                .collect(Collectors.toCollection(selectStatement::getColumns))
                .stream()
                .allMatch(column -> this.databaseMetadataCache.columnExists(column));

        // Validate selectStatement's criteria.
        areCriteriaValid(selectStatement); // Will throw exception instead of returning false.

        return true;
    }

    /**
     * Determines if the stmt's criteria are valid.  False is never actually returned - instead an exception will be thrown.
     * True will be returned if the criteria are valid.
     *
     * @return boolean
     * @throws Exception If the criteria is not valid or if the criteria is not clean SQL.
     */
    private boolean areCriteriaValid(SelectStatement selectStatement) throws Exception {
        for (Criterion criterion : selectStatement.getCriteria()) {
            if (! criterion.isValid()) {
                throw new Exception("This criteria is not valid:  " + criterion);
            }

            if (! sqlIsClean(criterion)) {
                throw new Exception("This criterion failed to be clean SQL:  " + criterion);
            }

            // Now that we know that the criteria's operator is not 'isNull' or 'isNotNull', we can assume that the
            // criteria's filter is needed.  Therefore, we should check if the filter is null or an empty string.
            // If so, throw an exception.
            if (! criterion.filterIsEmpty()) {
                int columnDataType = this.databaseMetadataCache.getColumnDataType(criterion.getColumn());
                boolean shouldHaveQuotes = isColumnQuoted(columnDataType);

                if (! shouldHaveQuotes && ! criterion.filterIsEmpty()) {
                    criterion.getFilterItems().forEach(filterItem -> {
                        if (! SqlCleanser.canParseNonQuotedFilter(filterItem)) {
                            throw new RuntimeException("The column is a number type, but the criteria's filter is not an number type: " + criterion);
                        }
                    });
                }
            }
        }

        return true;
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
     * @throws Exception If the data type is not supported.
     */
    public boolean isColumnQuoted(int dataType) throws Exception {
        Boolean isQuoted = Constants.TYPE_MAPPINGS.get(dataType); //todo:  make typeMappings a public static field in SelectStatementValidator so that it can be called?  Maybe even put it in Constants class because it's called by SelectStatementValidator and SqlBuilder?

        if (isQuoted == null) {
            throw new Exception(String.format("Data type, %s, is not recognized", dataType));
        }

        return isQuoted;
    }

}
