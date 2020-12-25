package net.querybuilder4j.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.sql_builder.SqlCleanser;

public class Column implements SqlRepresentation {

    private String fullyQualifiedName;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private int dataType;
    private String alias;

    public Column() {}

    public Column(String databaseName, String schemaName, String tableName, String columnName, int dataType, String alias) {
        this.fullyQualifiedName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        this.databaseName = databaseName;

        // Some databases, like SQLite, do not have schemas, so change the schema name to "null" instead of null because
        // SQLite primary keys (which is used for the cache) cannot have null values.
        this.schemaName = (schemaName == null) ? "null" : schemaName;

        this.tableName = tableName;
        this.columnName = columnName;
        this.dataType = dataType;
        this.alias = alias;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = (schemaName == null) ? "null" : schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        String s = "";
        try {
            s = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}

        return s;
    }

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (this.schemaName == null || this.schemaName.equals("null")) {
            if (hasAlias()) {
                return String.format(" %s%s%s.%s%s%s AS %s ",
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter,
                        this.alias);
            } else {
                return String.format(" %s%s%s.%s%s%s ",
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter);
            }
        } else {
            if (hasAlias()) {
                return String.format(" %s%s%s.%s%s%s.%s%s%s AS %s",
                        beginningDelimiter, SqlCleanser.escape(this.schemaName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter,
                        this.alias);
            } else {
                return String.format(" %s%s%s.%s%s%s.%s%s%s ",
                        beginningDelimiter, SqlCleanser.escape(this.schemaName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter);
            }

        }

    }

    /**
     * Allows you to set the `withAlias` flag based on whether you want the SQL string representation to include the
     * column alias.
     *
     * @param beginningDelimiter
     * @param endingDelimiter
     * @param withAlias
     * @return
     */
    public String toSql(char beginningDelimiter, char endingDelimiter, boolean withAlias) {
        if (withAlias) {
            return this.toSql(beginningDelimiter, endingDelimiter);
        } else {
            if (this.schemaName == null || this.schemaName.equals("null")) {
                return String.format(" %s%s%s.%s%s%s ",
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter);
            } else {
                return String.format(" %s%s%s.%s%s%s.%s%s%s ",
                        beginningDelimiter, SqlCleanser.escape(this.schemaName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter,
                        beginningDelimiter, SqlCleanser.escape(this.columnName), endingDelimiter);
            }
        }
    }

    private boolean hasAlias() {
        return this.alias != null && ! this.alias.equals("");
    }

}
