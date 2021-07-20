package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import net.querybuilder4j.util.Utils;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.querybuilder4j.dao.database.metadata.RedisDatabaseMetadataCacheDaoImpl.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedisDatabaseMetadataCacheDaoImplTest {

    private QbConfig qbConfig = buildQbConfigMock();

    private DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao = buildDatabaseMetadataCrawlerDaoMock();

    private Jedis jedis = buildJedisMock();

    private RedisDatabaseMetadataCacheDaoImpl redisDatabaseMetadataCacheDao = new RedisDatabaseMetadataCacheDaoImpl(
            this.qbConfig,
            this.databaseMetadataCrawlerDao,
            this.jedis
    );

    @Test
    public void refreshCache_callsJedisClientToCacheDatabaseMetadata() {
        this.redisDatabaseMetadataCacheDao.refreshCache();

        verify(this.jedis, times(1)).flushAll();
        verify(this.jedis, times(1)).select(DATABASE_REDIS_DB);
        verify(this.jedis, times(1)).select(SCHEMA_REDIS_DB);
        verify(this.jedis, times(1)).select(TABLE_REDIS_DB);
        verify(this.jedis, times(1)).select(COLUMN_REDIS_DB);
        verify(this.jedis, times(4)).set(anyString(), anyString()); // Jedis#set called for 1 database, 1 schema, 1 table, and 1 column.
    }

    @Test
    public void getDatabases_returnsDatabasesSuccessfully() {
        Set<Database> databases = this.redisDatabaseMetadataCacheDao.getDatabases();

        assertNotNull(databases);
        assertEquals(1, databases.size());
    }

    @Test
    public void findDatabases_findsDatabaseSuccessfully() {
        Database expectedDatabase = new Database("database0", DatabaseType.PostgreSQL);
        String databaseName = "database0";
        when(this.jedis.get(anyString()))
                .thenReturn(
                        Utils.serializeToJson(expectedDatabase)
                );

        Database resultingDatabase = this.redisDatabaseMetadataCacheDao.findDatabases(databaseName);

        verify(this.jedis, times(1)).select(DATABASE_REDIS_DB);
        verify(this.jedis, times(1)).get(databaseName);
        assertEquals(expectedDatabase.getDatabaseName(), resultingDatabase.getDatabaseName());
        assertEquals(expectedDatabase.getDatabaseType(), resultingDatabase.getDatabaseType());
    }

    @Test
    public void findSchemas_returnsSchemasSuccessfully() {
        String databaseName = "database0";
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Set.of(
                                "database0.schema0",
                                "database0.schema1"
                        )
                );
        when(this.jedis.mget(anyString(), anyString())) // anyString is passed in as an argument twice because there are 2 schemas.
                .thenReturn(
                        List.of(
                                Utils.serializeToJson(
                                        new Schema("database0", "schema0")
                                ),
                                Utils.serializeToJson(
                                        new Schema("database0", "schema1")
                                )
                        )
                );

        List<Schema> resultingSchemas = this.redisDatabaseMetadataCacheDao.findSchemas(databaseName);

        verify(this.jedis, times(1)).select(SCHEMA_REDIS_DB);
        verify(this.jedis, times(1)).keys(databaseName + ".*");
        assertEquals(2, resultingSchemas.size());
        resultingSchemas.forEach(schema -> assertEquals(databaseName, schema.getDatabaseName()));
    }

    @Test(expected = CacheMissException.class)
    public void findSchemas_noSchemaKeysFoundThrowsException() {
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Collections.emptySet()
                );

        this.redisDatabaseMetadataCacheDao.findSchemas("database0");
    }

    @Test
    public void findTables_returnsTablesSuccessfully() {
        String databaseName = "database0";
        String schemaName = "schema0";
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Set.of(
                                "database0.schema0.table0",
                                "database0.schema0.table1"
                        )
                );
        when(this.jedis.mget(anyString(), anyString()))
                .thenReturn(
                        List.of(
                                Utils.serializeToJson(
                                        new Table("database0", "schema0", "table0")
                                ),
                                Utils.serializeToJson(
                                        new Table("database0", "schema0", "table0")
                                )
                        )
                );

        List<Table> resultingTables = this.redisDatabaseMetadataCacheDao.findTables(databaseName, schemaName);

        verify(this.jedis, times(1)).select(TABLE_REDIS_DB);
        verify(this.jedis, times(1)).keys(databaseName + "." + schemaName + ".*");
        assertEquals(2, resultingTables.size());
        resultingTables.forEach(table -> {
            assertEquals(databaseName, table.getDatabaseName());
            assertEquals(schemaName, table.getSchemaName());
        });
    }

    @Test(expected = CacheMissException.class)
    public void findTables_noTableKeysFoundThrowsException() {
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Collections.emptySet()
                );

        this.redisDatabaseMetadataCacheDao.findTables("database0", "schema0");
    }

    @Test
    public void findColumns_returnsColumnsSuccessfully() {
        String databaseName = "database0";
        String schemaName = "schema0";
        String tableName = "table0";
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Set.of(
                                "database0.schema0.table0.column0",
                                "database0.schema0.table0.column1"
                        )
                );
        when(this.jedis.mget(anyString(), anyString()))
                .thenReturn(
                        List.of(
                                Utils.serializeToJson(
                                        new Column("database0", "schema0", "table0", "column0", Types.INTEGER, "alias")
                                ),
                                Utils.serializeToJson(
                                        new Column("database0", "schema0", "table0", "column1", Types.VARCHAR, "alias")
                                )
                        )
                );

        List<Column> resultingColumns = this.redisDatabaseMetadataCacheDao.findColumns(databaseName, schemaName, tableName);

        verify(this.jedis, times(1)).select(COLUMN_REDIS_DB);
        verify(this.jedis, times(1)).keys(databaseName + "." + schemaName + "." + tableName + ".*");
        assertEquals(2, resultingColumns.size());
        resultingColumns.forEach(column -> {
            assertEquals(databaseName, column.getDatabaseName());
            assertEquals(schemaName, column.getSchemaName());
            assertEquals(tableName, column.getTableName());
        });
    }

    @Test(expected = CacheMissException.class)
    public void findColumns_noColumnKeysFoundThrowsException() {
        when(this.jedis.keys(anyString()))
                .thenReturn(
                        Collections.emptySet()
                );

        this.redisDatabaseMetadataCacheDao.findColumns("database0", "schema0", "table0");
    }

    @Test
    public void getColumnDataType_getsColumnDataTypeSuccessfully() {
        Column column = TestUtils.buildColumn(Types.VARCHAR);
        when(this.jedis.get(anyString()))
                .thenReturn(
                        Utils.serializeToJson(column)
                );

        int resultingColumnDataType = this.redisDatabaseMetadataCacheDao.getColumnDataType(column);

        verify(this.jedis).select(COLUMN_REDIS_DB);
        verify(this.jedis).get(column.getFullyQualifiedName());
        assertEquals(column.getDataType(), resultingColumnDataType);
    }

    @Test
    public void columnExists_callsJedisClientExistsMethodAndReturnsResult() {
        Column column = TestUtils.buildColumn(Types.VARCHAR);
        when(this.jedis.exists(anyString()))
                .thenReturn(true);

        boolean columnExists = this.redisDatabaseMetadataCacheDao.columnExists(column);

        verify(this.jedis).select(COLUMN_REDIS_DB);
        verify(this.jedis).exists(column.getFullyQualifiedName());
        assertTrue(columnExists);
    }

    @Test
    public void columnsExist_callsJedisClientExistsMethodForEachColumnAndReturnsResult() {
        Column column1 = TestUtils.buildColumn(Types.INTEGER);
        Column column2 = TestUtils.buildColumn(Types.BIGINT);

        when(this.jedis.exists(anyString(), anyString()))
                .thenReturn(2L);

        boolean columnsExist = this.redisDatabaseMetadataCacheDao.columnsExist(
                List.of(
                        column1,
                        column2
                )
        );

        verify(this.jedis).select(COLUMN_REDIS_DB);
        verify(this.jedis).exists(
                Arrays.array(column1.getFullyQualifiedName(), column2.getFullyQualifiedName())
        );
        assertTrue(columnsExist);
    }

    @Test
    public void findByColumnName_callsJedisClientGetMethodAndReturnsResult() {
        Column expectedColumn = TestUtils.buildColumn(Types.BIGINT);
        when(this.jedis.get(anyString()))
                .thenReturn(
                        Utils.serializeToJson(expectedColumn)
                );

        Column resultingColumn = this.redisDatabaseMetadataCacheDao.findColumnByName(
                "database0",
                "schema0",
                "table0",
                "column0"
        );

        verify(this.jedis, times(1)).select(COLUMN_REDIS_DB);
        assertEquals(expectedColumn, resultingColumn);
    }

    private static DatabaseMetadataCrawlerDao buildDatabaseMetadataCrawlerDaoMock() {
        DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao = mock(DatabaseMetadataCrawlerDao.class);

        // Mock getSchemas return value.
        when(databaseMetadataCrawlerDao.getSchemas(any(QbConfig.TargetDataSource.class)))
                .thenReturn(
                        List.of(
                                new Schema("database0", "schema0")
                        )
                );

        // Mock getTablesAndViews return value.
        when(databaseMetadataCrawlerDao.getTablesAndViews(any(QbConfig.TargetDataSource.class), anyString()))
                .thenReturn(
                        List.of(
                                new Table("database0", "schema0", "table0")
                        )
                );

        // Mock getColumns return value.
        when(databaseMetadataCrawlerDao.getColumns(any(QbConfig.TargetDataSource.class), anyString(), anyString()))
                .thenReturn(
                        List.of(
                                new Column("database0", "schema0", "table0", "column0", Types.VARCHAR, "alias")
                        )
                );

        return databaseMetadataCrawlerDao;
    }

    private static Jedis buildJedisMock() {
        Jedis jedis = mock(Jedis.class);

        when(jedis.flushAll()).thenReturn("");
        when(jedis.select(anyInt())).thenReturn("");

        return jedis;
    }

    private static QbConfig buildQbConfigMock() {
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setDatabaseType(DatabaseType.MySql);
        targetDataSource1.setName("database1");
        targetDataSource1.setExcludeObjects(new QbConfig.TargetDataSource.ExcludeObjects());

        QbConfig qbConfig = mock(QbConfig.class);
        when(qbConfig.getTargetDataSources())
                .thenReturn(
                        List.of(
                                targetDataSource1
                        )
                );


        return qbConfig;
    }

}