package net.querybuilder4j.sql.builder;

import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqlBuilderFactoryTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private QueryTemplateService queryTemplateService;

    @InjectMocks
    private SqlBuilderFactory sqlBuilderFactory;

    @Before
    public void beforeEach() {
        when(this.databaseMetadataCacheDao.columnsExist(anyListOf(Column.class)))
                .thenReturn(true);
    }

    @Test(expected = CacheMissException.class)
    public void buildSqlBuilder_databaseNotFoundInCacheThrowsException() {
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(null);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(
                new Database("database", DatabaseType.PostgreSQL)
        );

        this.sqlBuilderFactory.buildSqlBuilder(selectStatement);
    }

    @Test
    public void buildSqlBuilder_mySql() {
        Database database = new Database("mySqlDatabase", DatabaseType.MySql);
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);

        assertTrue(sqlBuilder instanceof MySqlSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_oracle() {
        Database database = new Database("mySqlDatabase", DatabaseType.Oracle);
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);

        assertTrue(sqlBuilder instanceof OracleSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_postgreSql() {
        Database database = new Database("mySqlDatabase", DatabaseType.PostgreSQL);
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);

        assertTrue(sqlBuilder instanceof PostgresSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_sqlServer() {
        Database database = new Database("mySqlDatabase", DatabaseType.SqlServer);
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);

        assertTrue(sqlBuilder instanceof SqlServerSqlBuilder);
    }

    @Test
    public void buildSqlBuilder_sqlite() {
        Database database = new Database("mySqlDatabase", DatabaseType.Sqlite);
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(database);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setDatabase(database);
        selectStatement.getColumns().add(
                new Column("database", "schema", "table", "column", 4, "alias")
        );
        selectStatement.setTable(
                new Table("database", "schema", "table")
        );

        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);

        assertTrue(sqlBuilder instanceof SqliteSqlBuilder);
    }

}