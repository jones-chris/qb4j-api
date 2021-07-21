package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import net.querybuilder4j.exceptions.UncleanSqlException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Conjunction;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SqlValidatorTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Before
    public void beforeEach() {
        when(this.databaseMetadataCacheDao.columnsExist(anyListOf(Column.class)))
                .thenReturn(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertIsValid_emptyColumnsThrowsException() {
        SelectStatement selectStatement = new SelectStatement();

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertIsValid_nullTableThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = IllegalStateException.class)
    public void assertIsValid_criteriaColumnIsNullThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        null,
                        Operator.equalTo,
                        new Filter(List.of("1"), List.of(), List.of()),
                        null
                )
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = IllegalStateException.class)
    public void assertIsValid_criteriaOperatorIsNullThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(),
                        null,
                        new Filter(List.of("1"), List.of(), List.of()),
                        null
                )
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = IllegalStateException.class)
    public void assertIsValid_criteriaFilterValuesAndSubQueriesAreEmptyThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column(),
                        Operator.equalTo,
                        new Filter(List.of(), List.of(), List.of()),
                        null
                )
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = CriterionColumnDataTypeAndFilterMismatchException.class)
    public void assertIsValid_criteriaNumericalDataTypeCannotBeParsedThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 4, "alias"),
                        Operator.equalTo,
                        new Filter(List.of("bob"), List.of(), List.of()),
                        null
                )
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = CriterionColumnDataTypeAndFilterMismatchException.class)
    public void assertIsValid_criteriaBooleanDataTypeCannotBeParsedThrowsException() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );
        selectStatement.setTable(
                new Table()
        );
        selectStatement.getCriteria().add(
                new Criterion(
                        0,
                        null,
                        Conjunction.And,
                        new Column("database", "schema", "table", "column", 16, "alias"),
                        Operator.equalTo,
                        new Filter(List.of("bob"), List.of(), List.of()),
                        null
                )
        );

        SqlValidator.assertIsValid(selectStatement, this.databaseMetadataCacheDao);
    }

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_reservedOperatorThrowsException() {
        SqlValidator.assertSqlIsClean("+blah+blah");
    }

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_forbiddenMarksThrowsException() {
        SqlValidator.assertSqlIsClean("`blah`blah");
    }

    @Test(expected = UncleanSqlException.class)
    public void assertSqlIsClean_ansiKeywordsThrowsException() {
        SqlValidator.assertSqlIsClean("blahUPDATE blah blah");
    }

    @Test
    public void assertSqlIsClean_ansiKeywordsWithoutSurroundingWhitespaceReturnsTrue() {
        SqlValidator.assertSqlIsClean("blahUPDATEblah blah");
    }

}