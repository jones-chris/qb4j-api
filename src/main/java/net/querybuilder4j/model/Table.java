package net.querybuilder4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.sql_builder.SqlCleanser;

import java.util.ArrayList;
import java.util.List;

public class Table implements SqlRepresentation {

    private String fullyQualifiedName;
    private String databaseName;
    private String schemaName;
    private String tableName;
    private @JsonIgnore List<Column> columns = new ArrayList<>();

    public Table() {}

    public Table(String databaseName, String schemaName, String tableName) {
        this.fullyQualifiedName = String.format("%s.%s.%s", databaseName, schemaName, tableName);
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
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
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
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
            return String.format(" %s%s%s ",
                    beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter);
        } else {
            return String.format(" %s%s%s.%s%s%s ",
                    beginningDelimiter, SqlCleanser.escape(this.schemaName), endingDelimiter,
                    beginningDelimiter, SqlCleanser.escape(this.tableName), endingDelimiter);
        }
    }

}
