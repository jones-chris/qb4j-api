package net.querybuilder4j.cache;

import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Schema;
import net.querybuilder4j.model.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryDatabaseMetadataCacheImpl implements DatabaseMetadataCache {

    private final Qb4jConfig qb4jConfig;

    private Set<Database> cache = new HashSet<>();

    public Set<Database> getCache() {
        return cache;
    }

    public InMemoryDatabaseMetadataCacheImpl(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
        refreshCache();  // Populate cache on cache instantiation - which should occur at app start up.
    }

    /**
     * Run every 24 hours thereafter.  This method walks the qb4j target databases that are included in the Qb4jConfig.json
     * file and saves the target database metadata (databases, schemas, tables, and columns) to this class' `cache` field.
     * This class eager loads this metadata.
     *
     * @throws CacheRefreshException If an exception is raised when querying one of the target data sources.
     */
    @Override
    @Scheduled(fixedRate = 8640000000L)
    public void refreshCache() throws CacheRefreshException {
        // Get list of databases from qb4jConfig's target data sources.
        List<Database> databases = qb4jConfig.getTargetDataSources().stream()
                .map(targetDataSource -> new Database(targetDataSource.getName(), targetDataSource.getDatabaseType()))
                .collect(Collectors.toList());

        for (Database database : databases) {
            // Get schemas
            List<Schema> schemas = this.getSchemas(database.getDatabaseName());
            database.setSchemas(schemas);

            // Get tables
            for (Schema schema : database.getSchemas()) {
                List<Table> tables = this.getTablesAndViews(database.getDatabaseName(), schema.getSchemaName());
                schema.setTables(tables);

                // Get columns
                for (Table table : schema.getTables()) {
                    List<Column> columns = this.getColumns(database.getDatabaseName(), table.getSchemaName(), table.getTableName());
                    table.setColumns(columns);
                }
            }

        }

        // Save database metadata to cache
        cache.clear();
        cache.addAll(databases);
    }

    /**
     * A convenience method that makes it clearer that the cache is a {@link Set<Database>}.
     * @return {@link Set<Database>} The databases in the cache.
     */
    @Override
    public Set<Database> getDatabases() {
        return this.getCache();
    }

    @Override
    public Database findDatabases(String databaseName) {
        return this.cache.stream()
                .filter(database -> database.getDatabaseName().equals(databaseName))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public List<Schema> findSchemas(String databaseName) {
        return this.findDatabases(databaseName).getSchemas();
    }

    @Override
    public List<Table> findTables(String databaseName, String schemaName) {
        return this.findSchemas(databaseName).stream()
                .filter(schema -> schema.getSchemaName().equals(schemaName))
                .map(Schema::getTables)
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public List<Column> findColumns(String databaseName, String schemaName, String tableName) {
        return this.findTables(databaseName, schemaName).stream()
                .filter(table -> table.getTableName().equals(tableName))
                .map(Table::getColumns) // todo: sort alphabetically.
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public int getColumnDataType(Column column) {
        String databaseName = column.getDatabaseName();
        String schemaName = column.getSchemaName();
        String tableName = column.getTableName();
        String columnName = column.getColumnName();

        return this.findColumns(databaseName, schemaName, tableName).stream()
                .filter(col -> col.getColumnName().equals(columnName))
                .map(Column::getDataType)
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public boolean columnExists(Column column) {
        String databaseName = column.getDatabaseName();
        String schemaName = column.getSchemaName();
        String tableName = column.getTableName();
        String columnName = column.getColumnName();

        try {
            return this.findColumns(databaseName, schemaName, tableName).stream()
                    .anyMatch(col -> col.getColumnName().equals(columnName));
        } catch (Exception ex) {
            return false;
        }

    }

    @Override
    public Column findColumnByName(String databaseName, String schemaName, String tableName, String columnName) throws Exception {
        return this.findColumns(databaseName, schemaName, tableName)
                .stream()
                .filter(column -> column.getColumnName().equals(columnName))
                .findFirst()
                .orElseThrow(Exception::new);
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

}
