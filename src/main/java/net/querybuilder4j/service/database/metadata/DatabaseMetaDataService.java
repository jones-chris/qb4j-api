package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;

import java.util.List;
import java.util.Set;

public interface DatabaseMetaDataService {

    Set<Database> getDatabases();
    List<Schema> getSchemas(String databaseName) throws Exception;
    List<Table> getTablesAndViews(String databaseName, String schema) throws Exception;
    List<Column> getColumns(String databaseName, String schema, String table) throws Exception;

}
