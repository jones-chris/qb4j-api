package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostgresSqlBuilderTest extends SqlBuilderCommonTests {

    public PostgresSqlBuilderTest() {
        super();

        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.sqlBuilder = Mockito.spy(
                new PostgresSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService
                )
        );
        this.beginningDelimiter = '"';
        this.endingDelimiter = '"';
    }

}