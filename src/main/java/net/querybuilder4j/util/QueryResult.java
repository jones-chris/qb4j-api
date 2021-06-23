package net.querybuilder4j.util;

import lombok.Data;
import net.querybuilder4j.sql.statement.SelectStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended to take a {@link ResultSet} and SQL {@link String} and encapsulates the {@link ResultSet}'s
 * column names, data, and the SQL {@link String}.
 */
@Data
public class QueryResult {

    private SelectStatement selectStatement;

    private final List<String> columns = new ArrayList<>();

    private final List<Object[]> data = new ArrayList<>();

    private final String sql;

    public QueryResult(ResultSet resultSet, String sql) throws SQLException {
        // Set `sql` field.
        this.sql = sql;

        // Set `columns` and `data` fields.
        boolean columnNamesRetrieved = false;

        int totalColumns = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            // If column names have not been retrieved yet, build the json array before getting data.
            if (! columnNamesRetrieved) {
                for (int i = 0; i < totalColumns; i++) {
                    this.columns.add(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase());
                }

                columnNamesRetrieved = true;
            }

            // Now get the row's data, put it in a json array, and add it to the json object.
            Object[] newRow = new Object[totalColumns];
            for (int i = 0; i < totalColumns; i++) {
                newRow[i] = (resultSet.getObject(i + 1));
            }

            data.add(newRow);
        }
    }

}
