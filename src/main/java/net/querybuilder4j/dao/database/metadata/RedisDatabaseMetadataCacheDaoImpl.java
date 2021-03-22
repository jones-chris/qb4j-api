package net.querybuilder4j.dao.database.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.exceptions.CacheJsonDeserializationException;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import net.querybuilder4j.util.DatabaseMetadataCrawler;
import net.querybuilder4j.util.Utils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisDatabaseMetadataCacheDaoImpl implements DatabaseMetadataCacheDao {

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
     * The {@link Qb4jConfig} encapsulating the application context.
     */
    private final Qb4jConfig qb4jConfig;

    /**
     * The Redis/Jedis client.
     */
    private final Jedis jedis;

    public RedisDatabaseMetadataCacheDaoImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;

        // I know this constructor could just take `host` and `port` as parameters, but I think passing the Qb4jConfig
        // parameter makes it clearer that this class'/constructor's parameters originate from the Qb4jConfig bean and,
        // therefore, it's related qb4j.yml file.
        String host = this.qb4jConfig.getDatabaseMetadataCacheSource().getHost();
        int port = this.qb4jConfig.getDatabaseMetadataCacheSource().getPort();
        this.jedis = new Jedis(host, port);

        String password = this.qb4jConfig.getDatabaseMetadataCacheSource().getPassword();
        String username = this.qb4jConfig.getDatabaseMetadataCacheSource().getUsername();
        if (password != null) {
            if (username == null) {
                this.jedis.auth(password);
            } else {
                this.jedis.auth(username, password);
            }
        }

    }

    @Override
    public void refreshCache() throws Exception {
        // Clear all Redis databases.
        this.jedis.flushAll();

        // Loop through each database, get the schema, table/view, and column metadata and write it to Redis.
        // TODO:  Make this loop asynchronous for each database?
        for (Qb4jConfig.TargetDataSource targetDataSource : this.qb4jConfig.getTargetDataSources()) {
            // Write the database metadata to Redis.
            this.jedis.select(this.DATABASE_REDIS_DB);
            Database database = new Database(targetDataSource.getName(), targetDataSource.getDatabaseType());
            this.jedis.set(database.getFullyQualifiedName(), database.toString());

            // Get schema metadata and write to Redis.
            List<Schema> schemas = DatabaseMetadataCrawler.getSchemas(targetDataSource);
            this.jedis.select(this.SCHEMA_REDIS_DB);
            schemas.forEach(schema -> this.jedis.set(schema.getFullyQualifiedName(), schema.toString()));

            // Get tables metadata and write to Redis.
            for (Schema schema : schemas) {
                List<Table> tables = DatabaseMetadataCrawler.getTablesAndViews(targetDataSource, schema.getSchemaName());
                this.jedis.select(this.TABLE_REDIS_DB);
                tables.forEach(table -> this.jedis.set(table.getFullyQualifiedName(), table.toString()));

                // Get columns
                for (Table table : tables) {
                    List<Column> columns = DatabaseMetadataCrawler.getColumns(targetDataSource, table.getSchemaName(), table.getTableName());
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
        return Utils.deserializeJson(databaseJson, Database.class);
    }

    @Override
    public List<Schema> findSchemas(String databaseName) {
        this.jedis.select(this.SCHEMA_REDIS_DB);

        // Get all Redis keys that start with `{datbabaseName}.*` (notice the trailing period before `*`.
        String databaseKeyPattern = databaseName + ".*";
        Set<String> schemasRedisKeys = this.jedis.keys(databaseKeyPattern);

        if (schemasRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", databaseKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> schemasJson = this.jedis.mget(schemasRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(schemasJson, Schema.class);
    }

    @Override
    public List<Table> findTables(String databaseName, String schemaName) {
        this.jedis.select(this.TABLE_REDIS_DB);

        String tableKeyPattern = String.format("%s.%s.*", databaseName, schemaName);
        Set<String> tablesRedisKeys = this.jedis.keys(tableKeyPattern);

        if (tablesRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", tableKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> tablesJson = this.jedis.mget(tablesRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(tablesJson, Table.class);
    }

    @Override
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String columnKeyPattern = String.format("%s.%s.%s.*", databaseName, schemaName, tableName);
        Set<String> columnsRedisKeys = this.jedis.keys(columnKeyPattern);

        if (columnsRedisKeys.isEmpty()) {
            throw new CacheMissException(
                    String.format("Could not find %s", columnKeyPattern)
            );
        }

        // Get the values of the schemas redis keys.
        List<String> columnsJson = this.jedis.mget(columnsRedisKeys.toArray(new String[0]));

        return Utils.deserializeJsons(columnsJson, Column.class);
    }

    @Override
    public int getColumnDataType(Column column) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s",
                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
        );

        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        Column deserializedColumn = Utils.deserializeJson(columnJson, Column.class);
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
    public boolean columnsExist(List<Column> columns) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String[] fullyQualifiedColumnNames = (String[]) columns.stream()
                .map(column -> {
                    return String.format("%s.%s.%s.%s",
                            column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
                    );
                })
                .toArray();

        long numberOfExistingKeys = this.jedis.exists(fullyQualifiedColumnNames);

        return numberOfExistingKeys == fullyQualifiedColumnNames.length;
    }

    @Override
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) {
        this.jedis.select(this.COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        return Utils.deserializeJson(columnJson, Column.class);
    }

}
