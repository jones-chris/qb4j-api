package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.column.Column;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MySqlSqlBuilderTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private QueryTemplateService queryTemplateService;

    @InjectMocks
    @Spy
    private MySqlSqlBuilder mySqlSqlBuilder;

    @Before
    public void beforeEach() {
        when(this.databaseMetadataCacheDao.columnsExist(anyListOf(Column.class)))
                .thenReturn(true);
    }

    @Test
    public void constructor_setsFieldsCorrectly() {
        boolean result = SqlBuilderCommonTests.constructorSetsFieldsCorrectly(
                this.mySqlSqlBuilder,
                this.databaseMetadataCacheDao,
                this.queryTemplateService,
                '`',
                '`'
        );

        assertTrue(result);
    }

    @Test
    public void setStatement_setsFieldCorrectly() {
        boolean result = SqlBuilderCommonTests.setStatementSetsFieldsCorrectly(this.mySqlSqlBuilder);

        assertTrue(result);
    }

    @Test
    public void buildSql_callsAllSqlClauseGenerationMethods() {
        boolean result = SqlBuilderCommonTests.buildSqlCallsAllSqlClauseGenerationMethods(this.mySqlSqlBuilder);

        assertTrue(result);
    }

    @Test
    public void buildSql_sqlStringContainsBeginningAndEndingDelimitersCharacters() {
        boolean result = SqlBuilderCommonTests.buildSqlSqlStringContainsBeginningAndEndingDelimitersCharacters(
                this.mySqlSqlBuilder,
                "SELECT  `schema`.`table`.`column` AS alias  FROM  `schema`.`table`"
        );

        assertTrue(result);
    }

    @Test
    public void createCommonTableExpressionClause_emptyListResultsInEmptyStringBuilder() {
        boolean result = SqlBuilderCommonTests.createCommonTableExpressionClause_emptyListResultsInEmptyStringBuilder(this.mySqlSqlBuilder);

        assertTrue(result);
    }

}