package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import net.querybuilder4j.util.Utils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisDatabaseMetadataCacheDaoImpl implements DatabaseMetadataCacheDao {

    /**
     * The Redis database index that will hold database metadata.
     */
    static final int DATABASE_REDIS_DB = 0;

    /**
     * The Redis database index that will hold schema metadata.
     */
    static final int SCHEMA_REDIS_DB = 1;

    /**
     * The Redis database index that will hold table/view metadata.
     */
    static final int TABLE_REDIS_DB = 2;

    /**
     * The Redis database index that will hold column metadata.
     */
    static final int COLUMN_REDIS_DB = 3;

    /**
     * The {@link QbConfig} encapsulating the application context.
     */
    private final QbConfig qbConfig;

    /**
     * The Redis client.
     */
    private final Jedis jedis;

    /**
     * The class responsible for reading metadata from the target databases.
     */
    private final DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao;

    public RedisDatabaseMetadataCacheDaoImpl(
            QbConfig qbConfig,
            DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao,
            Jedis jedis
    ) {
        this.qbConfig = qbConfig;
        this.databaseMetadataCrawlerDao = databaseMetadataCrawlerDao;
        this.jedis = jedis;
    }

    @Override
    public void refreshCache() {
        // Clear all Redis databases.
        this.jedis.flushAll();

        // Loop through each database, get the schema, table/view, and column metadata and write it to Redis.
        // TODO:  Make this loop asynchronous for each database?
        for (QbConfig.TargetDataSource targetDataSource : this.qbConfig.getTargetDataSources()) {
            // Write the database metadata to Redis.
            this.jedis.select(DATABASE_REDIS_DB);
            Database database = new Database(targetDataSource.getName(), targetDataSource.getDatabaseType());
            this.jedis.set(database.getFullyQualifiedName(), database.toString());

            // Get schema metadata and write to Redis.
            List<Schema> schemas = this.databaseMetadataCrawlerDao.getSchemas(targetDataSource);
            this.jedis.select(SCHEMA_REDIS_DB);
            schemas.forEach(schema -> this.jedis.set(schema.getFullyQualifiedName(), schema.toString()));

            // Get tables metadata and write to Redis.
            for (Schema schema : schemas) {
                List<Table> tables = this.databaseMetadataCrawlerDao.getTablesAndViews(targetDataSource, schema.getSchemaName());
                this.jedis.select(TABLE_REDIS_DB);
                tables.forEach(table -> this.jedis.set(table.getFullyQualifiedName(), table.toString()));

                // Get columns
                for (Table table : tables) {
                    List<Column> columns = this.databaseMetadataCrawlerDao.getColumns(targetDataSource, table.getSchemaName(), table.getTableName());
                    this.jedis.select(COLUMN_REDIS_DB);
                    columns.forEach(column -> this.jedis.set(column.getFullyQualifiedName(), column.toString()));
                }
            }
        }

    }

    @Override
    public Set<Database> getDatabases() {
        return this.qbConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toSet());
    }

    @Override
    public Database findDatabases(String databaseName) {
        this.jedis.select(DATABASE_REDIS_DB);

        String databaseJson = this.jedis.get(databaseName);
        return Utils.deserializeJson(databaseJson, Database.class);
    }

    @Override
    public List<Schema> findSchemas(String databaseName) {
        this.jedis.select(SCHEMA_REDIS_DB);

        // Get all Redis keys that start with `{databaseName}.*` (notice the trailing period before `*`.
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
        this.jedis.select(TABLE_REDIS_DB);

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
        this.jedis.select(COLUMN_REDIS_DB);

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
        this.jedis.select(COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s",
                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
        );

        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        Column deserializedColumn = Utils.deserializeJson(columnJson, Column.class);
        return deserializedColumn.getDataType();
    }

    @Override
    public boolean columnExists(Column column) {
        this.jedis.select(COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s",
                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
        );
        return this.jedis.exists(fullyQualifiedColumnName);
    }

    @Override
    public boolean columnsExist(List<Column> columns) {
        this.jedis.select(COLUMN_REDIS_DB);

        String[] fullyQualifiedColumnNames = columns.stream()
                .map(column ->
                        String.format(
                                "%s.%s.%s.%s",
                                column.getDatabaseName(), column.getSchemaName(), column.getTableName(), column.getColumnName()
                        )
                )
                .toArray(String[]::new);

        long numberOfExistingKeys = this.jedis.exists(fullyQualifiedColumnNames);

        return numberOfExistingKeys == fullyQualifiedColumnNames.length;
    }

    @Override
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) {
        this.jedis.select(COLUMN_REDIS_DB);

        String fullyQualifiedColumnName = String.format("%s.%s.%s.%s", databaseName, schemaName, tableName, columnName);
        String columnJson = this.jedis.get(fullyQualifiedColumnName);
        return Utils.deserializeJson(columnJson, Column.class);
    }

}
