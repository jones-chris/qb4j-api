package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;

import java.util.List;

public interface DatabaseMetaDataService {

    List<Schema> getSchemas(String databaseName) throws Exception;
    List<Table> getTablesAndViews(String databaseName, String schema) throws Exception;
    List<Column> getColumns(String databaseName, String schema, String table) throws Exception;

}
