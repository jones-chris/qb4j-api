package net.querybuilder4j.dao.database.metadata;

import com.mysql.cj.jdbc.MysqlDataSource;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.exceptions.CacheRefreshException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMetadataCrawlerDaoTest {

    private final DataSource dataSource = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("h2_tables_seeder.sql")
            .build();

    private final DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao = new DatabaseMetadataCrawlerDao();

    private QbConfig.TargetDataSource targetDataSource;

    @Before
    public void beforeEach() {
        // Create a new target data source before each test so that any changes made to the target data source by each test
        // are removed before the next test is run.
        this.targetDataSource = this.buildTargetDataSource();
    }

    @Test
    public void getTargetDataSourceMetadata_returnsNonEmptyListOfDatabases() {
        List<QbConfig.TargetDataSource> targetDataSources = List.of(this.targetDataSource);

        List<Database> databases = this.databaseMetadataCrawlerDao.getTargetDataSourceMetadata(targetDataSources);

        assertNotNull(databases);
        assertEquals(1, databases.size());
        assertEquals(2, databases.get(0).getSchemas().size());
        // todo:  Add more assertions for tables and columns once we know how many schemas the H2 database has.
    }

    @Test
    public void getSchemas_returnsNonEmptyListOfSchemas() {
        List<Schema> schemas = this.databaseMetadataCrawlerDao.getSchemas(targetDataSource);

        assertNotNull(schemas);
        assertEquals(2, schemas.size());
    }

    @Test(expected = CacheRefreshException.class)
    public void getSchemas_whenSqlExceptionIsRaisedThenCacheRefreshExceptionIsThrown() {
        QbConfig.TargetDataSource fakeTargetDataSource = this.buildFakeTargetDataSource();

        this.databaseMetadataCrawlerDao.getSchemas(fakeTargetDataSource);
    }

    @Test
    public void getSchemas_excludingInformationSchema() {
        QbConfig.TargetDataSource.ExcludeObjects excludeObjects = new QbConfig.TargetDataSource.ExcludeObjects();
        excludeObjects.setSchemas(
                List.of("information_schema")
        );
        when(this.targetDataSource.getExcludeObjects())
                .thenReturn(excludeObjects);

        List<Schema> schemas = this.databaseMetadataCrawlerDao.getSchemas(this.targetDataSource);

        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        assertNotEquals("information_schema", schemas.get(0).getSchemaName());
    }

    @Test
    public void getSchemas_ifAllSchemasAreExcludedThenOneSchemaWithNullStringForTheNameIsReturned() {
        QbConfig.TargetDataSource.ExcludeObjects excludeObjects = new QbConfig.TargetDataSource.ExcludeObjects();
        excludeObjects.setSchemas(
                List.of(
                        "information_schema",
                        "public"
                )
        );
        when(this.targetDataSource.getExcludeObjects())
                .thenReturn(excludeObjects);

        List<Schema> schemas = this.databaseMetadataCrawlerDao.getSchemas(this.targetDataSource);

        assertNotNull(schemas);
        assertEquals(1, schemas.size());
        assertEquals("null", schemas.get(0).getSchemaName());
    }

    @Test
    public void getTablesAndViews_returnsNonEmptyListOfTables() {
        List<Table> tables = this.databaseMetadataCrawlerDao.getTablesAndViews(targetDataSource, "PUBLIC");

        assertNotNull(tables);
        assertEquals(2, tables.size());
    }

    @Test(expected = CacheRefreshException.class)
    public void getTablesAndViews_whenSqlExceptionIsRaisedThenCacheRefreshExceptionIsThrown() {
        QbConfig.TargetDataSource fakeTargetDataSource = this.buildFakeTargetDataSource();

        this.databaseMetadataCrawlerDao.getTablesAndViews(fakeTargetDataSource, "fake schema");
    }

    @Test
    public void getTablesAndViews_excludingCustomersTable() {
        QbConfig.TargetDataSource.ExcludeObjects excludeObjects = new QbConfig.TargetDataSource.ExcludeObjects();
        excludeObjects.setTables(
                List.of("public.customers")
        );
        when(this.targetDataSource.getExcludeObjects())
                .thenReturn(excludeObjects);

        List<Table> tables = this.databaseMetadataCrawlerDao.getTablesAndViews(this.targetDataSource, "PUBLIC");

        assertNotNull(tables);
        assertEquals(1, tables.size());
        assertEquals("public", tables.get(0).getSchemaName());
        assertNotEquals("customers", tables.get(0).getTableName());
    }

    @Test
    public void getColumns_returnsNonEmptyListOfColumns() {
        List<Column> columns = this.databaseMetadataCrawlerDao.getColumns(
                this.targetDataSource,
                "PUBLIC",
                "CUSTOMERS"
        );

        assertNotNull(columns);
        assertEquals(7, columns.size());
    }

    @Test(expected = CacheRefreshException.class)
    public void getColumn_whenSqlExceptionIsRaisedThenCacheRefreshExceptionIsThrown() {
        QbConfig.TargetDataSource fakeTargetDataSource = this.buildFakeTargetDataSource();

        this.databaseMetadataCrawlerDao.getColumns(
                fakeTargetDataSource,
                "fake schema",
                "fake table"
        );
    }

    @Test
    public void getColumns_excludeCustomersIdColumn() {
        QbConfig.TargetDataSource.ExcludeObjects excludeObjects = new QbConfig.TargetDataSource.ExcludeObjects();
        excludeObjects.setColumns(
                List.of("public.customers.id")
        );
        when(this.targetDataSource.getExcludeObjects())
                .thenReturn(excludeObjects);

        List<Column> columns = this.databaseMetadataCrawlerDao.getColumns(
                this.targetDataSource,
                "PUBLIC",
                "CUSTOMERS"
        );

        assertNotNull(columns);
        assertEquals(6, columns.size());
        columns.forEach(column -> {
            assertEquals("public", column.getSchemaName());
            assertEquals("customers", column.getTableName());
            assertNotEquals("id", column.getColumnName());
        });
    }

    private QbConfig.TargetDataSource buildTargetDataSource() {
        QbConfig.TargetDataSource targetDataSource = mock(QbConfig.TargetDataSource.class);
        when(targetDataSource.getDatabaseType())
                .thenReturn(DatabaseType.MySql);
        when(targetDataSource.getName())
                .thenReturn("database1");
        when(targetDataSource.getExcludeObjects())
                .thenReturn(new QbConfig.TargetDataSource.ExcludeObjects());
        when(targetDataSource.getDataSource())
                .thenReturn(this.dataSource);

        return targetDataSource;
    }

    private QbConfig.TargetDataSource buildFakeTargetDataSource() {
        // This TargetDataSource will not return a valid DataSource when getDataSource() is called.
        QbConfig.TargetDataSource fakeTargetDataSource = mock(QbConfig.TargetDataSource.class);
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL("jdbc:mysql://localhost:3306/bobDb");
        dataSource.setUser("bob");
        dataSource.setPassword("bob's password");
        when(fakeTargetDataSource.getDataSource())
                .thenReturn(dataSource);

        return fakeTargetDataSource;
    }

}