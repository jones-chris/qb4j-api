package net.querybuilder4j.model;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMetadata {

    private List<String> schemas = new ArrayList<>();
    private List<String> tables = new ArrayList<>();
    private List<String> columns = new ArrayList<>();

    public DatabaseMetadata() {}

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

}
