package net.querybuilder4j.service.query_template;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.dao.query_template.QueryTemplateDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueryTemplateServiceImplTest {

    @Spy
    private QueryTemplateDao queryTemplateDao;

    @Spy
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @InjectMocks
    private QueryTemplateServiceImpl queryTemplateService;

    // In-memory surrogate "database".
    private final List<SelectStatement> queryTemplates = new ArrayList<>();

    @Before
    public void beforeEach() {
        this.queryTemplates.clear();
    }

    @Test
    public void save_noCurrentVersionOfQueryTemplateSetsVersionToZero() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        when(this.queryTemplateDao.getNewestVersion(anyString()))
                .thenReturn(Optional.empty());
        when(this.queryTemplateDao.save(any(SelectStatement.class)))
                .thenAnswer(answer -> {
                    this.queryTemplates.add(selectStatement); // Add select statement to in-memory surrogate "database".
                    return true;
                });

        this.queryTemplateService.save(selectStatement);

        assertEquals(1, this.queryTemplates.size());
        assertEquals(0, this.queryTemplates.get(0).getMetadata().getVersion());
    }

    @Test
    public void save_existingCurrentVersionOfQueryTemplateSetsVersionToExistingCurrentVersionPlusOne() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        when(this.queryTemplateDao.getNewestVersion(anyString()))
                .thenReturn(Optional.of(0));
        when(this.queryTemplateDao.save(any(SelectStatement.class)))
                .thenAnswer(answer -> {
                    this.queryTemplates.add(selectStatement); // Add select statement to in-memory surrogate "database".
                    return true;
                });

        this.queryTemplateService.save(selectStatement);

        assertEquals(1, this.queryTemplates.size());
        assertEquals(1, this.queryTemplates.get(0).getMetadata().getVersion());
    }

    @Test(expected = NullPointerException.class)
    public void save_selectStatementParameterIsNullThrowsException() {
        this.queryTemplateService.save(null);
    }

    @Test(expected = NullPointerException.class)
    public void save_selectStatementMetadataIsNullThrowsException() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(null);

        this.queryTemplateService.save(selectStatement);
    }

    @Test
    public void save_ifSelectStatementLimitIsNullThenDefaultLimitIsUsed() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        selectStatement.setLimit(null);

        this.queryTemplateService.save(selectStatement);

        assertNotNull(selectStatement.getLimit());
        assertNotEquals(0, selectStatement.getMetadata().getMaxNumberOfRowsReturned()); // maxNumberOfRowsReturned is a primitive long, so just check that it's not 0.
    }

    @Test
    public void save_ifSelectStatementLimitIsNotNullThenLimitIsUnchanged() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        long limit = 10L;
        selectStatement.setLimit(limit);

        this.queryTemplateService.save(selectStatement);

        assertEquals(limit, selectStatement.getLimit().longValue());
        assertNotEquals(0, selectStatement.getMetadata().getMaxNumberOfRowsReturned()); // maxNumberOfRowsReturned is a primitive long, so just check that it's not 0.
    }

    @Test
    public void save_ifSelectStatementHasCriteriaThenCriteriaParametersMetadataIsSet() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        selectStatement.getCriteria().add(
                TestUtils.buildCriterion(
                        TestUtils.buildColumn(Types.VARCHAR),
                        new Filter(
                                List.of(),
                                List.of(),
                                List.of("parameter1")
                        )
                )
        );
        when(this.databaseMetadataCacheDao.columnExists(any(Column.class)))
                .thenReturn(true);

        this.queryTemplateService.save(selectStatement);

        assertEquals(1, selectStatement.getMetadata().getCriteriaParameters().size());
    }

    @Test(expected = CacheMissException.class)
    public void save_ifSelectStatementHasCriteriaAndCriterionColumnCannotBeFoundInCacheThenExceptionIsThrow() {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        selectStatement.getCriteria().add(
                TestUtils.buildCriterion(
                        TestUtils.buildColumn(Types.VARCHAR),
                        new Filter(
                                List.of(),
                                List.of(),
                                List.of("parameter1")
                        )
                )
        );
        when(this.databaseMetadataCacheDao.columnExists(any(Column.class)))
                .thenReturn(false);

        this.queryTemplateService.save(selectStatement);
    }

    @Test
    public void findByName_callsDaoMethodAndReturnsResult() {
        SelectStatement expectedSelectStatement = TestUtils.buildSelectStatement();
        when(this.queryTemplateDao.findByName(anyString(), anyInt()))
                .thenReturn(expectedSelectStatement);

        SelectStatement resultingSelectStatement = this.queryTemplateService.findByName("name", 0);

        verify(this.queryTemplateDao, times(1))
                .findByName(anyString(), anyInt());
        assertEquals(expectedSelectStatement, resultingSelectStatement);
    }

    @Test
    public void getNames_callsDaoMethodAndReturnsResult() {
        List<String> expectedNames = List.of("bob", "joe", "sam");
        when(this.queryTemplateDao.listNames())
                .thenReturn(expectedNames);

        List<String> resultingNames = this.queryTemplateService.getNames();

        verify(this.queryTemplateDao, times(1))
                .listNames();
        assertEquals(expectedNames, resultingNames);
    }

    @Test
    public void getCommonTableExpressionsSelectStatement_criteriaArgumentsAndCommonTableExpressionSelectStatementAreSet() {
        SelectStatement expectedSelectStatement = TestUtils.buildSelectStatement();
        when(this.queryTemplateDao.findByName(anyString(), anyInt()))
                .thenReturn(expectedSelectStatement);
        List<CommonTableExpression> commonTableExpressions = List.of(
                this.buildCommonTableExpression("name1", "queryName1", 0),
                this.buildCommonTableExpression("name2", "queryName2", 0)
        );

        this.queryTemplateService.getCommonTableExpressionSelectStatement(commonTableExpressions);

        commonTableExpressions.forEach(commonTableExpression -> {
            assertNotNull(commonTableExpression.getSelectStatement());

            SelectStatement selectStatement = commonTableExpression.getSelectStatement();

            assertEquals(1, selectStatement.getCriteriaArguments().size());
        });
    }

    @Test
    public void getVersions_callsDaoMethodAndPassesResultBack() {
        List<Integer> expectedVersions = List.of(1, 2, 3);
        when(this.queryTemplateDao.getVersions(anyString()))
                .thenReturn(expectedVersions);

        List<Integer> resultingVersions = this.queryTemplateService.getVersions("name");

        verify(this.queryTemplateDao, times(1)).getVersions(anyString());
        assertEquals(expectedVersions, resultingVersions);
    }

    @Test
    public void getMetadata_callsDaoMethodAndPassesResultBack() {
        SelectStatement.Metadata expectedMetadata = new SelectStatement.Metadata();
        when(this.queryTemplateDao.getMetadata(anyString(), anyInt()))
                .thenReturn(expectedMetadata);

        SelectStatement.Metadata resultingMetadata = this.queryTemplateService.getMetadata("name", 0);

        verify(this.queryTemplateDao, times(1))
                .getMetadata(anyString(), anyInt());
        assertEquals(expectedMetadata, resultingMetadata);
    }

    private CommonTableExpression buildCommonTableExpression(String name, String queryName, int version) {
        CommonTableExpression commonTableExpression = new CommonTableExpression();
        commonTableExpression.setName(name);
        commonTableExpression.setQueryName(queryName);
        commonTableExpression.setVersion(version);
        commonTableExpression.setParametersAndArguments(
                Map.of(
                        "parameter1",
                        List.of(
                                "argument1",
                                "argument2"
                        )
                )
        );

        return commonTableExpression;
    }

}