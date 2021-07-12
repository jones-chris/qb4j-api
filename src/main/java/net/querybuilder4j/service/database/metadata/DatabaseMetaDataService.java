package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;

import java.util.List;
import java.util.Set;

public interface DatabaseMetaDataService {

    Set<Database> getDatabases();
    List<Schema> getSchemas(String databaseName);
    List<Table> getTablesAndViews(String databaseName, String schema);
    List<Column> getColumns(String databaseName, String schema, String table) ;

}
