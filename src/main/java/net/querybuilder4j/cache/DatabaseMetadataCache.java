package net.querybuilder4j.cache;

import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;

import java.util.List;
import java.util.Set;

public interface DatabaseMetadataCache {

    void refreshCache() throws Exception;
    Set<Database> getDatabases();
    Database findDatabases(String databaseName);
    List<Schema> findSchemas(String databaseName);
    List<Table> findTables(String databaseName, String schemaName);
    List<Column> findColumns(String databaseName, String schemaName, String tableName);
    int getColumnDataType(Column column);
    boolean columnExists(Column column);
    Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) throws Exception;

}
