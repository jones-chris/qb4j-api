package net.querybuilder4j.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.constants.DatabaseType;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private @JsonIgnore String fullyQualifiedName;
    private String databaseName;
    private DatabaseType databaseType;
    private @JsonIgnore List<Schema> schemas = new ArrayList<>();

    public Database() {}

    public Database(String databaseName, DatabaseType databaseType) {
        this.fullyQualifiedName = String.format("%s", databaseName);
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }

    public String getFullyQualifiedName() {
        return this.fullyQualifiedName;
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

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public List<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        String s = "";
        try {
            s = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}

        return s;
    }

}
