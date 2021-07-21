package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MySqlSqlBuilderTest extends SqlBuilderCommonTests {

    public MySqlSqlBuilderTest() {
        super();

        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.sqlBuilder = Mockito.spy(
                new MySqlSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService
                )
        );
        this.beginningDelimiter = '`';
        this.endingDelimiter = '`';
    }

}