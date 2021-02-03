package net.querybuilder4j.sql.statement;

import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FilterTest {

    @Test
    public void isEmpty_allPropertiesAreEmptyReturnsFalse() {
        Filter filter = new Filter();

        assertTrue(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAndSubQueriesAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));
        filter.setSubQueries(List.of("world"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void isEmpty_valuesAndSubQueriesAndParametersAreNotEmptyReturnsFalse() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));
        filter.setSubQueries(List.of("world"));
        filter.setParameters(List.of("!"));

        assertFalse(filter.isEmpty());
    }

    @Test
    public void hasSubQueries_subQueriesAreEmptyReturnsFalse() {
        Filter filter = new Filter();

        assertFalse(filter.hasSubQueries());
    }

    @Test
    public void hasSubQueries_subQueriesAreNotEmptyReturnsTrue() {
        Filter filter = new Filter();
        filter.setSubQueries(List.of("hello"));

        assertTrue(filter.hasSubQueries());
    }

    @Test
    public void toSql_nonEmptyValuesFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello", "world"));

        String actualSql = filter.toSql(Operator.in);

        assertEquals("(hello, world)", actualSql);
    }

    @Test
    public void toSql_emptyValuesFormattedCorrectly() {
        Filter filter = new Filter();

        String actualSql = filter.toSql(Operator.in);

        assertEquals("", actualSql);
    }

    @Test
    public void toSql_equalToFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.equalTo);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_notEqualToFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.notEqualTo);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_greaterThanFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.greaterThan);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_lessThanFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.lessThan);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_greaterThanOrEqualFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.greaterThanOrEquals);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_lessThanOrEqualFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.lessThanOrEquals);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_likeFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.like);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_notLikeFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.notLike);

        assertEquals("(hello)", actualSql);
    }

    @Test
    public void toSql_inFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello", "world"));

        String actualSql = filter.toSql(Operator.in);

        assertEquals("(hello, world)", actualSql);
    }

    @Test
    public void toSql_notInFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello", "world"));

        String actualSql = filter.toSql(Operator.notIn);

        assertEquals("(hello, world)", actualSql);
    }

    @Test
    public void toSql_isNullFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.isNull);

        assertEquals("", actualSql);
    }

    @Test
    public void toSql_isNotNullFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.isNotNull);

        assertEquals("", actualSql);
    }

    // todo:  change this expected string once BETWEEN and NOT BETWEEN are supported.
    @Test
    public void toSql_betweenFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.between);

        assertEquals("(hello)", actualSql);
    }

    // todo:  change this expected string once BETWEEN and NOT BETWEEN are supported.
    @Test
    public void toSql_notBetweenFormattedCorrectly() {
        Filter filter = new Filter();
        filter.setValues(List.of("hello"));

        String actualSql = filter.toSql(Operator.notBetween);

        assertEquals("(hello)", actualSql);
    }

}