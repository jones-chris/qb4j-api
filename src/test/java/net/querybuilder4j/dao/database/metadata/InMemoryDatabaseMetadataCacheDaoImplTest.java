package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryDatabaseMetadataCacheDaoImplTest {

    private QbConfig qbConfig = buildQbConfigMock();

    private static DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao = mock(DatabaseMetadataCrawlerDao.class);

    private InMemoryDatabaseMetadataCacheDaoImpl inMemoryDatabaseMetadataCacheDao = new InMemoryDatabaseMetadataCacheDaoImpl(
                    this.qbConfig,
                    databaseMetadataCrawlerDao
            );

    @BeforeClass
    public static void beforeAll() {
        when(databaseMetadataCrawlerDao.getTargetDataSourceMetadata(anyList()))
                .thenReturn(
                        buildDatabaseMetadata(2, 2, 2, 2)
                );
    }

    @Test
    public void constructor_populatesCache() {
        assertFalse(this.inMemoryDatabaseMetadataCacheDao.getCache().isEmpty());
    }

    @Test
    public void getDatabases_returnsCache() {
        Set<Database> expectedDatabases = this.inMemoryDatabaseMetadataCacheDao.getCache();

        Set<Database> resultingDatabases = this.inMemoryDatabaseMetadataCacheDao.getDatabases();

        assertEquals(expectedDatabases, resultingDatabases);
    }



    @Test
    public void findDatabases_findsDatabaseSuccessfully() {
        final String databaseName = "database1";

        Database database = this.inMemoryDatabaseMetadataCacheDao.findDatabases(databaseName);

        assertNotNull(database);
        assertEquals(databaseName, database.getDatabaseName());
    }

    @Test(expected = CacheMissException.class)
    public void findDatabases_exceptionIsThrownIfNoDatabaseIsFound() {
        final String databaseName = "You won't find this database name";

        this.inMemoryDatabaseMetadataCacheDao.findDatabases(databaseName);
    }

    @Test
    public void findSchemas_returnsSchemasSuccessfully() {
        final String databaseName = "database1";

        List<Schema> schemas = this.inMemoryDatabaseMetadataCacheDao.findSchemas(databaseName);

        assertNotNull(schemas);
        assertFalse(schemas.isEmpty());
        schemas.forEach(schema -> assertEquals(databaseName, schema.getDatabaseName()));
    }

    @Test
    public void findTables_returnsTablesSuccessfully() {
        final String databaseName = "database1";
        final String schemaName = "schema1";

        List<Table> tables = this.inMemoryDatabaseMetadataCacheDao.findTables(databaseName, schemaName);

        assertNotNull(tables);
        assertFalse(tables.isEmpty());
        tables.forEach(table -> {
            assertEquals(databaseName, table.getDatabaseName());
            assertEquals(schemaName, table.getSchemaName());
        });
    }

    @Test(expected = CacheMissException.class)
    public void findTables_exceptionIsThrownIfNoTableIsFound() {
        this.inMemoryDatabaseMetadataCacheDao.findTables("fake database", "fake schema");
    }

    @Test
    public void findColumns_returnsColumnsSuccessfully() {
        final String databaseName = "database1";
        final String schemaName = "schema1";
        final String tableName = "table1";

        List<Column> columns = this.inMemoryDatabaseMetadataCacheDao.findColumns(databaseName, schemaName, tableName);

        assertNotNull(columns);
        assertFalse(columns.isEmpty());
        columns.forEach(column -> {
            assertEquals(databaseName, column.getDatabaseName());
            assertEquals(schemaName, column.getSchemaName());
            assertEquals(tableName, column.getTableName());
        });
    }

    @Test(expected = CacheMissException.class)
    public void findColumns_exceptionIsThrownIfNoColumnIsFound() {
        this.inMemoryDatabaseMetadataCacheDao.findColumns("fake database", "fake schema", "fake table");
    }

    @Test
    public void getColumnDataType_returnsColumnDataTypeSuccessfully() {
        Column column = new Column("database0", "schema0", "table0", "column1", Types.INTEGER, "alias");

        int dataType = this.inMemoryDatabaseMetadataCacheDao.getColumnDataType(column);

        // Every column is an INTEGER data type per the buildDatabaseMetadata method, so just assert that the INTEGER data type was retrieved.
        assertEquals(Types.INTEGER, dataType);
    }

    @Test(expected = CacheMissException.class)
    public void getColumnDataType_throwsExceptionIfNoColumnFound() {
        Column column = new Column("fake database", "fake schema", "fake table", "fake column", Types.INTEGER, "alias");

        this.inMemoryDatabaseMetadataCacheDao.getColumnDataType(column);
    }

    @Test
    public void columnExists_returnsTrueIfColumnExists() {
        Column column = new Column("database0", "schema0", "table0", "column1", Types.INTEGER, "alias");

        boolean columnExists = this.inMemoryDatabaseMetadataCacheDao.columnExists(column);

        assertTrue(columnExists);
    }

    @Test
    public void columnExists_returnsFalseIfColumnDoesNotExist() {
        Column column = new Column("database0", "schema0", "table0", "fake column", Types.INTEGER, "alias");

        boolean columnExists = this.inMemoryDatabaseMetadataCacheDao.columnExists(column);

        assertFalse(columnExists);
    }

    @Test
    public void columnsExist_returnsTrueIfAllColumnsExist() {
        List<Column> columns = List.of(
                new Column("database0", "schema0", "table0", "column0", Types.INTEGER, "alias"),
                new Column("database0", "schema0", "table0", "column1", Types.INTEGER, "alias")
        );

        boolean columnsExist = this.inMemoryDatabaseMetadataCacheDao.columnsExist(columns);

        assertTrue(columnsExist);
    }

    @Test
    public void columnsExist_returnsFalseIfOneOrMoreColumnsDoNotExist() {
        List<Column> columns = List.of(
                new Column("database0", "schema0", "table0", "column0", Types.INTEGER, "alias"),
                new Column("database0", "schema0", "table0", "fake column", Types.INTEGER, "alias")
        );

        boolean columnsExist = this.inMemoryDatabaseMetadataCacheDao.columnsExist(columns);

        assertFalse(columnsExist);
    }

    @Test
    public void findColumnByName_returnsColumnSuccessfullyIfItExists() {
        final String databaseName = "database0";
        final String schemaName = "schema0";
        final String tableName = "table0";
        final String columnName = "column0";

        Column column = this.inMemoryDatabaseMetadataCacheDao.findColumnByName(
                databaseName,
                schemaName,
                tableName,
                columnName
        );

        assertNotNull(column);
        assertEquals(databaseName, column.getDatabaseName());
        assertEquals(schemaName, column.getSchemaName());
        assertEquals(tableName, column.getTableName());
        assertEquals(columnName, column.getColumnName());
    }

    @Test(expected = CacheMissException.class)
    public void findColumnByName_throwsExceptionIfColumnDoesNotExist() {
        final String databaseName = "database0";
        final String schemaName = "schema0";
        final String tableName = "table0";
        final String columnName = "fake column";

        this.inMemoryDatabaseMetadataCacheDao.findColumnByName(
                databaseName,
                schemaName,
                tableName,
                columnName
        );
    }

    private static QbConfig buildQbConfigMock() {
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setDatabaseType(DatabaseType.MySql);
        targetDataSource1.setName("database1");
        targetDataSource1.setExcludeObjects(new QbConfig.TargetDataSource.ExcludeObjects());

        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setDatabaseType(DatabaseType.PostgreSQL);
        targetDataSource2.setName("database2");
        targetDataSource2.setExcludeObjects(new QbConfig.TargetDataSource.ExcludeObjects());

        QbConfig qbConfig = mock(QbConfig.class);
        when(qbConfig.getTargetDataSources())
                .thenReturn(
                        List.of(
                                targetDataSource1,
                                targetDataSource2
                        )
                );


        return qbConfig;
    }

    /**
     * A convenience method that builds a {@link Set<Database>} given a number of databases, schemas, tables, and columns
     * to build.
     *
     * @param numberOfDatabases The number of {@link Database}s to build.
     * @param numberOfSchemas The number of {@link Schema}s that each {@link Database} should have.
     * @param numberOfTables The number of {@link Table}s that each {@link Schema} should have.
     * @param numberOfColumns THe number of {@link Column}s that each {@link Table} should have.
     * @return {@link List<Database>}
     */
    private static List<Database> buildDatabaseMetadata(
            int numberOfDatabases,
            int numberOfSchemas,
            int numberOfTables,
            int numberOfColumns
    ) {
        List<Database> databases = new ArrayList<>();

        // For each database...
        IntStream.range(0, numberOfDatabases)
                .forEach(databaseNumber -> {
                    String databaseName = "database" + databaseNumber;
                    Database database = new Database(databaseName, DatabaseType.PostgreSQL);
                    databases.add(database);

                    // For each schema...
                    IntStream.range(0, numberOfSchemas)
                            .forEach(schemaNumber -> {
                                String schemaName = "schema" + schemaNumber;
                                Schema schema = new Schema(databaseName, schemaName);
                                database.getSchemas().add(schema);

                                // For each table...
                                IntStream.range(0, numberOfTables)
                                        .forEach(tableNumber -> {
                                            String tableName = "table" + tableNumber;
                                            Table table = new Table(databaseName, schemaName, tableName);
                                            schema.getTables().add(table);

                                            // For each column...
                                            IntStream.range(0, numberOfColumns)
                                                    .forEach(columnNumber -> {
                                                        String columnName = "column" + columnNumber;
                                                        Column column = new Column(databaseName, schemaName, tableName, columnName, Types.INTEGER, "alias");
                                                        table.getColumns().add(column);
                                                    });
                                        });
                            });
                });

        return databases;
    }

}