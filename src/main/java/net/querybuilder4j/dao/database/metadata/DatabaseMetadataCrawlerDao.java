package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.exceptions.CacheRefreshException;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for crawling a {@link List<net.querybuilder4j.config.QbConfig.TargetDataSource>} and building
 * a {@link Set<Database>} from the metadata.
 */
@Repository
public class DatabaseMetadataCrawlerDao {

    /**
     * A convenience method that crawls/traverses the {@link net.querybuilder4j.config.QbConfig.TargetDataSource}s that are passed into it and returns
     * a {@link List<Database>} with the database metadata.  This method is the equivalent of calling the {@link this#getSchemas(QbConfig.TargetDataSource)},
     * {@link this#getTablesAndViews(QbConfig.TargetDataSource, String)}, and {@link this#getColumns(QbConfig.TargetDataSource, String, String)}
     * if you desire a {@link List<Database>} encapsulating all this metadata.  If you would like to store the data in a
     * different structure, then call each of these methods separately.
     *
     * @param targetDataSources A {@link List<net.querybuilder4j.config.QbConfig.TargetDataSource>} to retrieve database
     *                          metadata for.
     * @return {@link List<Database>}
     */
    public List<Database> getTargetDataSourceMetadata(List<QbConfig.TargetDataSource> targetDataSources) {
        List<Database> databases = new ArrayList<>();

        for (QbConfig.TargetDataSource targetDataSource : targetDataSources) {
            // Get schemas
            List<Schema> schemas = getSchemas(targetDataSource);
            Database database = new Database(targetDataSource.getName(), targetDataSource.getDatabaseType());
            database.setSchemas(schemas);

            // Get tables
            for (Schema schema : database.getSchemas()) {
                List<Table> tables = getTablesAndViews(targetDataSource, schema.getSchemaName());
                schema.setTables(tables);

                // Get columns
                for (Table table : schema.getTables()) {
                    List<Column> columns = getColumns(targetDataSource, table.getSchemaName(), table.getTableName());
                    table.setColumns(columns);
                }
            }

            databases.add(database);
        }

        return databases;
    }

    /**
     * Queries the target SQL database for schemas (excluding schemas defined in the `excludeObjects#schemas` of the
     * {@link QbConfig.TargetDataSource#getExcludeObjects()#getSchemas(String)}) as defined in the {@link QbConfig}
     * and instantiates a {@link Schema} for each schema the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.QbConfig.TargetDataSource} to query for schema metadata.
     * @return {@link List <Schema>} A list of the database schemas.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public List<Schema> getSchemas(QbConfig.TargetDataSource targetDataSource) throws CacheRefreshException {
        List<Schema> schemas = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                schemaName = (schemaName == null) ? "null" : schemaName.toLowerCase();
                Schema schema = new Schema(databaseName, schemaName);

                // Add the schema if it is not an excluded schema.
                if (! targetDataSource.getExcludeObjects().getSchemas().contains(schemaName)) {
                    schemas.add(schema);
                }
            }

            // If no schemas exist (which is the case for some databases, like SQLite), add a schema with null for
            // the schema name.
            if (schemas.isEmpty()) {
                schemas.add(new Schema(databaseName, "null"));
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return schemas;
    }

    /**
     * Queries the target SQL database for tables and views (excluding tables and views defined in the
     * {@link QbConfig.TargetDataSource#getExcludeObjects()#getTablesAndViews(String, String)} (String)}) as defined in
     * the {@link QbConfig} and instantiates a {@link Table} for each table and view the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.QbConfig.TargetDataSource} to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @return {@link List<Table>} A list of the database tables and views.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public List<Table> getTablesAndViews(QbConfig.TargetDataSource targetDataSource, String schema) throws CacheRefreshException {
        List<Table> tables = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, schema, null, new String[] {"TABLE", "VIEW"});

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                schemaName = (schemaName == null) ? "null" : schemaName.toLowerCase();
                String tableName = rs.getString("TABLE_NAME").toLowerCase();

                // Add the table if it is not an excluded table.
                if (! targetDataSource.getExcludeObjects().getTables().contains(schemaName + "." + tableName)) {
                    Table table = new Table(databaseName, schemaName, tableName);
                    tables.add(table);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return tables;
    }

    /**
     * Queries the target SQL database for columns (excluding columns defined in the
     * {@link QbConfig.TargetDataSource#getExcludeObjects()#getColumns(String, String, String)}) as defined in
     * the {@link QbConfig} and instantiates a {@link Column} for each column the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.QbConfig.TargetDataSource} to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @param table The name of the table or view to query for column metatdata.
     * @return {@link List<Column>} A list of the database columns.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public List<Column> getColumns(QbConfig.TargetDataSource targetDataSource, String schema, String table) throws CacheRefreshException {
        List<Column> columns = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                schemaName = (schemaName == null) ? "null" : schemaName.toLowerCase();
                String tableName = rs.getString("TABLE_NAME").toLowerCase();
                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                int dataType = rs.getInt("DATA_TYPE");

                // Add the column if it is not an excluded column.
                if (! targetDataSource.getExcludeObjects().getColumns().contains(schemaName + "." + tableName + "." + columnName)) {
                    Column column = new Column(databaseName, schemaName, tableName, columnName, dataType, null);
                    columns.add(column);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return columns;
    }

}
