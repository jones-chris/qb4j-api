package net.querybuilder4j.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryResult {

    private List<String> columns = new ArrayList<>();

    private List<Object[]> data = new ArrayList<>();

    private String sql;

    public List<String> getColumns() {
        return columns;
    }

    public List<Object[]> getData() {
        return data;
    }

    public String getSql() {
        return sql;
    }

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
