package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;

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
    boolean columnsExist(List<Column> columns);
    Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) throws Exception;

}
