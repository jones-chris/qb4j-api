package net.querybuilder4j.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisDatabaseMetadataCacheImpl implements DatabaseMetadataCache {

    /**
     * The Redis database index that will hold database metadata.
     */
    private final int DATABASE_REDIS_DB = 0;

    /**
     * The Redis database index that will hold schema metadata.
     */
    private final int SCHEMA_REDIS_DB = 1;

    /**
     * The Redis database index that will hold table/view metadata.
     */
    private final int TABLE_REDIS_DB = 2;

    /**
     * The Redis database index that will hold column metadata.
     */
    private final int COLUMN_REDIS_DB = 3;

    /**
     * An object mapper that can be used by the {@link this#deserializeJson(String, Class)} and
     * {@link this#deserializeJsons(Iterable, Class)} methods.  I opted to make this a class field so that these methods
     * do not have to constantly instantiate an {@link ObjectMapper} over and over again.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The {@link Qb4jConfig} encapsulating the application context.
     */
    private final Qb4jConfig qb4jConfig;

    /**
     * The Redis/Jedis client.
     */
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
        // Get list of databases from qb4jConfig's target data sources.
        List<Database> databases = qb4jConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toList());

        // Clear the currently selected Redis database.
        this.jedis.flushAll();

        // Loop through each database, get the schema, table/view, and column metadata and write it to Redis.
        // TODO:  Make this loop asynchronous for each database?
        for (Database database : databases) {
            // Write the database metadata to Redis.
            this.jedis.select(this.DATABASE_REDIS_DB);
            this.jedis.set(database.getFullyQualifiedName(), database.toString());

            // Get schema metadata and write to Redis.
            List<Schema> schemas = this.getSchemas(database.getDatabaseName());
            this.jedis.select(this.SCHEMA_REDIS_DB);
            schemas.forEach(schema -> this.jedis.set(schema.getFullyQualifiedName(), schema.toString()));

            // Get tables metadata and write to Redis.
            for (Schema schema : database.getSchemas()) {
                List<Table> tables = this.getTablesAndViews(database.getDatabaseName(), schema.getSchemaName());
                this.jedis.select(this.TABLE_REDIS_DB);
                tables.forEach(table -> this.jedis.set(table.getFullyQualifiedName(), table.toString()));

                // Get columns
                for (Table table : schema.getTables()) {
                    List<Column> columns = this.getColumns(database.getDatabaseName(), table.getSchemaName(), table.getTableName());
                    this.jedis.select(this.COLUMN_REDIS_DB);
                    columns.forEach(column -> this.jedis.set(column.getFullyQualifiedName(), column.toString()));
                }
            }
        }

    }

    @Override
    public Set<Database> getDatabases() {
        return this.qb4jConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toSet());
    }

    @Override
    public Database findDatabases(String databaseName) {
        this.jedis.select(this.DATABASE_REDIS_DB);

        String databaseJson = this.jedis.get(databaseName);
        return this.deserializeJson(databaseJson, Database.class);
    }

    @Override
    public List<Schema> findSchemas(String databaseName) {
        this.jedis.select(this.SCHEMA_REDIS_DB);

        // Get all Redis values that start with `{datbabaseName}.*` (notice the trailing period before `*`.
        ScanResult<String> scanResult = this.jedis.scan(
                "0",
                new ScanParams().match(databaseName + ".*")
        );
        List<String> schemasJson = scanResult.getResult();
        return this.deserializeJsons(schemasJson, Schema.class);
    }

    @Override
    public List<Table> findTables(String databaseName, String schemaName) {
        this.jedis.select(this.TABLE_REDIS_DB);

        ScanResult<String> scanResult = this.jedis.scan(
                "0",
                new ScanParams().match(String.format("%s.%s.*", databaseName, schemaName))
        );
        List<String> tablesJson = scanResult.getResult();
        return this.deserializeJsons(tablesJson, Table.class);
    }

    @Override
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        ScanResult<String> scanResult = this.jedis.scan(
                "0",
                new ScanParams().match(String.format("%s.%s.%s.*", databaseName, schemaName, tableName))
        );
        List<String> columnsJson = scanResult.getResult();
        return this.deserializeJsons(columnsJson, Column.class);
    }

    @Override
    public int getColumnDataType(Column column) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s",
                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
        );

        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        Column deserializedColumn = this.deserializeJson(columnJson, Column.class);
        return deserializedColumn.getDataType();
    }

    @Override
    public boolean columnExists(Column column) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s",
                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
        );
        return this.jedis.exists(fullyQualifiedColumnName);
    }

    @Override
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        return this.deserializeJson(columnJson, Column.class);
    }

    /**
     * Queries the target SQL database for schemas (excluding schemas defined in the `excludeObjects#schemas` of the
     * {@link Qb4jConfig.TargetDataSource#getExcludeObjects()#getSchemas(String)}) as defined in the {@link Qb4jConfig}
     * and instantiates a {@link Schema} for each schema the query returns.
     * @param databaseName The name of the database to query for schema metadata.
     * @return {@link List<Schema>} A list of the database schemas.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    private List<Schema> getSchemas(String databaseName) throws CacheRefreshException {
        List<Schema> schemas = new ArrayList<>();
        Qb4jConfig.TargetDataSource targetDataSource = qb4jConfig.getTargetDataSource(databaseName);

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
     * @param databaseName The name of the database to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @return {@link List<Table>} A list of the database tables and views.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
    private List<Table> getTablesAndViews(String databaseName, String schema) throws CacheRefreshException {
        List<Table> tables = new ArrayList<>();
        Qb4jConfig.TargetDataSource targetDataSource = qb4jConfig.getTargetDataSource(databaseName);

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
     * @param databaseName The name of the database to query for table and view metadata.
     * @param schema The name of the schema to query for table and view metadata.
     * @param table The name of the table or view to query for column metatdata.
     * @return {@link List<Column>} A list of the database columns.
     * @throws CacheRefreshException If a {@link SQLException} is thrown while querying the database.
     */
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
                if (! targetDataSource.getExcludeObjects().getColumns().contains(column.getFullyQualifiedName().toLowerCase())) {
                    columns.add(column);
                }
            }

        } catch (SQLException e) {
            throw new CacheRefreshException(e);
        }

        return columns;
    }

    /**
     * A convenience method for instantiating a {@link T} from a JSON {@link String}.
     * @param json The JSON {@link String}.
     * @param clazz The class to instantiate from the JSON {@link String}.
     * @param <T> The class to instantiate from the JSON {@link String}.
     * @return An instance of {@link T}.
     */
    private <T> T deserializeJson(String json, Class<T> clazz) {
        try {
            return this.objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            /*
             * Instantiate and throw a child of the RuntimeException class so we don't have to worry about exception checking
             * in the event that a JSON string cannot be deserialized because the application is not expected to recover
             * from that.
             */
            throw new CacheJsonDeserializationException(e);
        }
    }

    /**
     * A convenience method for instantiating a {@link List<T>} from a {@link Iterable<String>}.
     * @param jsons The {@link Iterable<String>} of JSON {@link String}s.
     * @param clazz The class to instantiate from each of the JSON {@link String}s.
     * @param <T> The class to instantiate from each of the JSON {@link String}s.
     * @return A {@link List<T>}.
     */
    private <T> List<T> deserializeJsons(Iterable<String> jsons, Class<T> clazz) {
        List<T> deserializedObjects = new ArrayList<>();
        jsons.forEach(json -> deserializedObjects.add(
                this.deserializeJson(json, clazz)
        ));

        return deserializedObjects;
    }

}
