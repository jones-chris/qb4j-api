package net.querybuilder4j.util;

import net.querybuilder4j.cache.CacheRefreshException;
import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMetadataCrawler {

    /**
     * Queries the target SQL database for schemas (excluding schemas defined in the `excludeObjects#schemas` of the
     * {@link Qb4jConfig.TargetDataSource#getExcludeObjects()#getSchemas(String)}) as defined in the {@link Qb4jConfig}
     * and instantiates a {@link Schema} for each schema the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.Qb4jConfig.TargetDataSource} to query for schema metadata.
     * @return {@link List <Schema>} A list of the database schemas.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public static List<Schema> getSchemas(Qb4jConfig.TargetDataSource targetDataSource) throws CacheRefreshException {
        List<Schema> schemas = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                Schema schema = new Schema(databaseName, (schemaName == null) ? "null" : schemaName);

                // Add the schema if it is not an excluded schema.
                if (! targetDataSource.getExcludeObjects().getSchemas().contains(schema.getSchemaName().toLowerCase())) {
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
     * {@link Qb4jConfig.TargetDataSource#getExcludeObjects()#getTablesAndViews(String, String)} (String)}) as defined in
     * the {@link Qb4jConfig} and instantiates a {@link Table} for each table and view the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.Qb4jConfig.TargetDataSource} to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @return {@link List<Table>} A list of the database tables and views.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public static List<Table> getTablesAndViews(Qb4jConfig.TargetDataSource targetDataSource, String schema) throws CacheRefreshException {
        List<Table> tables = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, schema, null, new String[] {"TABLE", "VIEW"});

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                Table table = new Table(databaseName, (schemaName == null) ? "null" : schemaName, tableName);

                // Add the table if it is not an excluded table.
                if (! targetDataSource.getExcludeObjects().getTables().contains(table.getFullyQualifiedName().toLowerCase())) {
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
     * {@link Qb4jConfig.TargetDataSource#getExcludeObjects()#getColumns(String, String, String)}) as defined in
     * the {@link Qb4jConfig} and instantiates a {@link Column} for each column the query returns.
     * @param targetDataSource The {@link net.querybuilder4j.config.Qb4jConfig.TargetDataSource} to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @param table The name of the table or view to query for column metatdata.
     * @return {@link List<Column>} A list of the database columns.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    public static List<Column> getColumns(Qb4jConfig.TargetDataSource targetDataSource, String schema, String table) throws CacheRefreshException {
        List<Column> columns = new ArrayList<>();
        String databaseName = targetDataSource.getName();

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");

                Column column = new Column(databaseName, schemaName, tableName, columnName, dataType, null);

                // Add the column if it is not an excluded column.
                if (! targetDataSource.getExcludeObjects().getColumns().contains(column.getFullyQualifiedName().toLowerCase())) {
                    columns.add(column);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return columns;
    }

}
