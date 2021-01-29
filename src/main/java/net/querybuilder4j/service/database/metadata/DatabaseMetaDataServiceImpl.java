package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Service
public class DatabaseMetaDataServiceImpl implements DatabaseMetaDataService {

    private DatabaseMetadataCache databaseMetadataCache;

    @Autowired
    public DatabaseMetaDataServiceImpl(DatabaseMetadataCache databaseMetadataCache) {
        this.databaseMetadataCache = databaseMetadataCache;
    }

    @Override
    public Set<Database> getDatabases() {
        return this.databaseMetadataCache.getDatabases();
    }

    /**
     *
     * @param databaseName
     * @return
     * @throws Exception
     */
    @Override
    public List<Schema> getSchemas(String databaseName) throws Exception {
        return this.databaseMetadataCache.findSchemas(databaseName);
    }

    /**
     *
     * @param databaseName
     * @param schemaName
     * @return
     * @throws Exception
     */
    @Override
    public List<Table> getTablesAndViews(String databaseName, String schemaName) throws Exception {
        return this.databaseMetadataCache.findTables(databaseName, schemaName);
    }

    /**
     * Because this service gets data from a SQLite database and SQLite does not have a concise SQL query for getting all table
     * columns, I have to write Java code to concatenate the table columns with the table name.
     *
     * @param databaseName
     * @param schemaName
     * @param tableName
     * @return A list of the
     * @throws SQLException
     */
    @Override
    public List<Column> getColumns(String databaseName, String schemaName, String tableName) throws Exception {
        return this.databaseMetadataCache.findColumns(databaseName, schemaName, tableName);
    }

}
