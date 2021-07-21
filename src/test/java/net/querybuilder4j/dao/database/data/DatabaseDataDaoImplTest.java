package net.querybuilder4j.dao.database.data;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.QueryFailureException;
import net.querybuilder4j.sql.builder.SqlBuilder;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.util.QueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseDataDaoImplTest {

    @Mock
    private QbConfig qbConfig;

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private SqlBuilderFactory sqlBuilderFactory;

    @InjectMocks
    private DatabaseDataDaoImpl databaseDataDao;

    @Before
    public void beforeEach() {
        when(this.qbConfig.getTargetDataSourceAsDataSource(anyString()))
            .thenReturn(
                    new EmbeddedDatabaseBuilder()
                            .setType(EmbeddedDatabaseType.H2)
                            .build()
            );
    }

//        @BeforeClass
//        public static void beforeClass() throws IOException {
//                InputStream inputStream = DatabaseDataDaoImplTest.class.getClassLoader().getResourceAsStream("qb.yml");
//                assert inputStream != null;
//                String qbConfig = new String(inputStream.readAllBytes());
//
////                System.setProperty("qbConfig", qbConfig);
//
//        }
    @Test
    public void executeQuery_runsSuccessfullyAgainstDatabase() {
        QueryResult queryResult = this.databaseDataDao.executeQuery("database", "SELECT CURRENT_TIMESTAMP");

        assertNotNull(queryResult);
        assertEquals(1, queryResult.getData().size());
    }

    @Test(expected = QueryFailureException.class)
    public void executeQuery_throwsQueryFailureExceptionWhenSqlExceptionIsThrown() {
        this.databaseDataDao.executeQuery("database", "THIS QUERY WILL FAIL");
    }

    @Test
    public void getColumnMembers_returnsQueryResult() {
        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(
                        new Database("database", DatabaseType.MySql)
                );
        when(this.databaseMetadataCacheDao.findColumnByName(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(
                        TestUtils.buildColumn(Types.INTEGER)
                );
        SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        when(this.sqlBuilderFactory.buildSqlBuilder(any(SelectStatement.class)))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.buildSql())
                .thenReturn("SELECT CURRENT_TIMESTAMP");

        QueryResult queryResult = this.databaseDataDao.getColumnMembers(
                "database",
                "schema",
                "table",
                "column",
                10,
                0,
                true,
                "%mySearchText%"
        );

        assertNotNull(queryResult);
        assertEquals(1, queryResult.getData().size());
    }

}