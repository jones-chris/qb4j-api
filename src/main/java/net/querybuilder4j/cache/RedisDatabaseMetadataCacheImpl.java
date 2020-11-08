package net.querybuilder4j.cache;

import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisDatabaseMetadataCacheImpl implements DatabaseMetadataCache {

    private Qb4jConfig qb4jConfig;

    private final Jedis jedis;

    public RedisDatabaseMetadataCacheImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;

        // I know this constructor could just take `host` and `port` as parameters, but I think passing the Qb4jConfig
        // parameter makes it clearer that this class'/constructor's parameters originate from the Qb4jConfig bean and,
        // therefore, it's related qb4j.yml file.
        String host = this.qb4jConfig.getDatabaseMetadataCacheSource().getHost();
        int port = this.qb4jConfig.getDatabaseMetadataCacheSource().getPort();
        this.jedis = new Jedis(host, port);
    }

    @Override
    public void refreshCache() throws Exception {
        System.out.println("hello");
//        // Get list of databases from qb4jConfig's target data sources.
//        List<Database> databases = qb4jConfig.getTargetDataSources().stream()
//                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
//                .collect(Collectors.toList());
//
//        for (Database database : databases) {
//            // Get schemas
//            List<Schema> schemas = this.getSchemas(database.getDatabaseName());
//            database.setSchemas(schemas);
//
//            // Get tables
//            for (Schema schema : database.getSchemas()) {
//                List<Table> tables = this.getTablesAndViews(database.getDatabaseName(), schema.getSchemaName());
//                schema.setTables(tables);
//
//                // Get columns
//                for (Table table : schema.getTables()) {
//                    List<Column> columns = this.getColumns(database.getDatabaseName(), table.getSchemaName(), table.getTableName());
//                    table.setColumns(columns);
//                }
//            }
//
//        }
//
//        // Save database metadata to cache
//        cache.clear();
//        cache.addAll(databases);
    }

    @Override
    public Set<Database> getDatabases() {
        return null;
    }

    @Override
    public Database findDatabases(String databaseName) {
        return null;
    }

    @Override
    public List<Schema> findSchemas(String databaseName) {
        return null;
    }

    @Override
    public List<Table> findTables(String databaseName, String schemaName) {
        return null;
    }

    @Override
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        return null;
    }

    @Override
    public int getColumnDataType(Column column) {
        return 0;
    }

    @Override
    public boolean columnExists(Column column) {
        return false;
    }

    @Override
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) throws Exception {
        return null;
    }

    private List<Schema> getSchemas(String databaseName) throws CacheRefreshException {
        List<Schema> schemas = new ArrayList<>();
        Qb4jConfig.TargetDataSource targetDataSource = qb4jConfig.getTargetDataSource(databaseName);

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                Schema schema = new Schema(databaseName, (schemaName == null) ? "null" : schemaName);

                // Add the schema if it is not an excluded schema.
                if (! targetDataSource.getExcludeObjects().getSchemas().contains(schema.getSchemaName())) {
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

    private List<Table> getTablesAndViews(String databaseName, String schema) throws CacheRefreshException {
        List<Table> tables = new ArrayList<>();
        Qb4jConfig.TargetDataSource targetDataSource = qb4jConfig.getTargetDataSource(databaseName);

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, schema, null, null);

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                Table table = new Table(databaseName, (schemaName == null) ? "null" : schemaName, tableName);

                // Add the table if it is not an excluded table.
                if (! targetDataSource.getExcludeObjects().getTables().contains(table.getFullyQualifiedName())) {
                    tables.add(table);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return tables;
    }

    private List<Column> getColumns(String databaseName, String schema, String table) throws CacheRefreshException {
        List<Column> columns = new ArrayList<>();
        Qb4jConfig.TargetDataSource targetDataSource = qb4jConfig.getTargetDataSource(databaseName);

        try (Connection conn = targetDataSource.getDataSource().getConnection()) {
            ResultSet rs = conn.getMetaData().getColumns(null, schema, table, "%");

            while (rs.next()) {
                String schemaName = rs.getString("TABLE_SCHEM");
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");

                Column column = new Column(databaseName, schemaName, tableName, columnName, dataType, null);

                // Add the column if it is not an excluded column.
                if (! targetDataSource.getExcludeObjects().getColumns().contains(column.getFullyQualifiedName())) {
                    columns.add(column);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return columns;
    }

}
