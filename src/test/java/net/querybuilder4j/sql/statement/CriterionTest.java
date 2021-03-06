package net.querybuilder4j.sql.statement;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CriterionTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Test
    public void hasSearchOperator_trueForLikeOperator() {
        Column column = createMockColumn("test", false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, null, column, Operator.like, filter, null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_trueForNotLikeOperator() {
        Column column = createMockColumn("test", false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, null, column, Operator.notLike, filter, null);

        assertTrue(criterion.hasSearchOperator());
    }

    @Test
    public void hasSearchOperator_falseForEqualToOperator() {
        Column column = createMockColumn("test",false);
        Filter filter = new Filter(List.of("hello%"), List.of(), List.of());
        Criterion criterion = new Criterion(0,null, null, column, Operator.equalTo, filter, null);

        assertFalse(criterion.hasSearchOperator());
    }

//    @Test
//    public void isValid_trueForNotNullOperatorWithEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of(""), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.isNotNull, filter, null);
//
//        assertTrue(criterion.isValid());
//    }
//
//    @Test
//    public void isValid_falseForEqualToOperatorWithEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of(""), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.equalTo, filter, null);
//
//        assertFalse(criterion.isValid());
//    }
//
//    @Test
//    public void isValid_trueForEqualToOperatorWithNonEmptyStringFilter() {
//        Column column = createMockColumn("test",true);
//        Filter filter = new Filter(List.of("test"), List.of(), List.of());
//        Criterion criterion = new Criterion(0, null, null, column, Operator.equalTo, filter, null);
//
//        assertTrue(criterion.isValid());
//    }

    @Test
    public void toSql_nullSchema() {
        Column column = createMockColumn(null, true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nullStringSchema() {
        Column column = createMockColumn("null", true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void toSql_nonNullSchema() {
        Column column = createMockColumn("my_schema", true);
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        Criterion criterion = new Criterion(0, null, Conjunction.And, column, Operator.equalTo, filter, null);
        String expectedSql = " AND `my_schema`.`test`.`test` = (test) ";

        String actualSql = criterion.toSql('`', '`');

        assertEquals(expectedSql, actualSql);
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     */
    @Test
    public void toSqlDeep_oneBranchWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column, null);
        Criterion childCriterion = createMockCriterion(rootCriterion, column, null);
        rootCriterion.getChildCriteria().add(childCriterion);
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test')) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     *      - child2
     */
    @Test
    public void toSqlDeep_twoBranchesWithOneNodeDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column, null);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column, null);
        Criterion childCriterion2 = createMockCriterion(rootCriterion, column, null);
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test')   AND `test`.`test` = ('test')) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root
     *      - child1
     *          - child1_1
     */
    @Test
    public void toSqlDeep_oneBranchWithTwoNodesDeep() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column, null);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column, null);
        Criterion childCriterion1_1 = createMockCriterion(childCriterion1, column, null);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        rootCriterion.setChildCriteria(Collections.singletonList(childCriterion1));
        String expectedSql = "  (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root
     *      - child1
     *          - child1_1
     *      - child2
     *          - child2_1
     */
    @Test
    public void toSqlDeep_oneBranchWithTwoNestedBranches() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion = createMockCriterion(null, column, null);
        Criterion childCriterion1 = createMockCriterion(rootCriterion, column, null);
        Criterion childCriterion1_1 = createMockCriterion(childCriterion1, column, null);
        childCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        Criterion childCriterion2 = createMockCriterion(rootCriterion, column, null);
        Criterion childCriterion2_1 = createMockCriterion(childCriterion2, column, null);
        childCriterion2.setChildCriteria(Collections.singletonList(childCriterion2_1));
        rootCriterion.setChildCriteria(Arrays.asList(childCriterion1, childCriterion2));
        String expectedSql = "  (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                Collections.singletonList(rootCriterion),
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    /**
     * From a UI perspective, this test looks like this:
     *
     * - root1
     *      - child1
     * - root2
     *      - child2
     *          - child2_1
     */
    @Test
    public void toSqlDeep_twoRoots() {
        Column column = createMockColumn("null", true);
        Criterion rootCriterion1 = createMockCriterion(null,  column, null);
        Criterion rootCriterion2 = createMockCriterion(null, column, Conjunction.And);
        Criterion childCriterion1_1 = createMockCriterion(rootCriterion1, column, null);
        Criterion childCriterion2_1 = createMockCriterion(rootCriterion2, column, null);
        Criterion childCriterion2_1_1 = createMockCriterion(childCriterion2_1, column, null);
        childCriterion2_1.setChildCriteria(Collections.singletonList(childCriterion2_1_1));
        rootCriterion1.setChildCriteria(Collections.singletonList(childCriterion1_1));
        rootCriterion2.setChildCriteria(Collections.singletonList(childCriterion2_1));
        List<Criterion> rootCriteria = Arrays.asList(rootCriterion1, rootCriterion2);
        String expectedSql = "  (`test`.`test` = ('test')   AND `test`.`test` = ('test'))   AND (`test`.`test` = ('test')   AND (`test`.`test` = ('test')   AND `test`.`test` = ('test'))) ";

        CriteriaTreeFlattener criteriaTreeFlattener = new CriteriaTreeFlattener(
                rootCriteria,
                this.databaseMetadataCacheDao
        );

        assertEquals(expectedSql, criteriaTreeFlattener.getSqlStringRepresentation('`', '`'));
    }

    private Column createMockColumn(String schema, boolean hasSingleQuotedColumn) {
        int dataType = (hasSingleQuotedColumn) ? Types.VARCHAR : Types.INTEGER;
        return new Column("test", schema, "test", "test", dataType, null);
    }

    private Criterion createMockCriterion(Criterion parentCriterion, Column column, Conjunction parentCriterionConjunction) {
        // If no parentCriterion parameter, then return a root criterion.
        // Else, return a child criterion.
        Filter filter = new Filter(List.of("test"), List.of(), List.of());
        if (parentCriterion == null) {
            return new Criterion(0, null, parentCriterionConjunction, column, Operator.equalTo, filter, null);
        } else {
            return new Criterion(0, parentCriterion, Conjunction.And, column, Operator.equalTo, filter, null);
        }
    }

}
