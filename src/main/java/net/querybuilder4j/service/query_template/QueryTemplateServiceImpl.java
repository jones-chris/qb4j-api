package net.querybuilder4j.service.query_template;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.dao.query_template.QueryTemplateDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.criterion.CriterionParameter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

    private QueryTemplateDao queryTemplateDao;

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Autowired
    public QueryTemplateServiceImpl(QueryTemplateDao queryTemplateDao, DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        this.queryTemplateDao = queryTemplateDao;
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        // Create metadata for criteria parameters.
        this.setSelectStatementCriteriaParameters(selectStatement);

        // Get the newest version number in the database.
        this.queryTemplateDao.getNewestVersion(selectStatement.getMetadata().getName())
                .ifPresentOrElse(
                        (currentVersion) -> selectStatement.getMetadata().setVersion(currentVersion + 1), // If version exists, increment by 1.
                        () -> selectStatement.getMetadata().setVersion(0) // If version does not exist, set to 0.
                );

        // Save the Select Statement.
        return queryTemplateDao.save(selectStatement);
    }

    @Override
    public Map<String, SelectStatement> findByNames(List<String> names) {
        return this.queryTemplateDao.findByNames(names);
    }

    @Override
    public SelectStatement findByName(String name, int version) {
        return queryTemplateDao.findByName(name, version);
    }

    @Override
    public List<String> getNames() {
        return queryTemplateDao.listNames();
    }

    /**
     * This method is responsible for retrieving a {@link SelectStatement} for each of the
     * {@link CommonTableExpression}s in the parameter, commonTableExpressions.
     *
     * @param commonTableExpressions {@link List<CommonTableExpression>}
     */
    @Override
    public void getCommonTableExpressionSelectStatement(List<CommonTableExpression> commonTableExpressions) {
        if (! commonTableExpressions.isEmpty()) {
            List<String> commonTableExpressionNames = commonTableExpressions.stream()
                    .map(CommonTableExpression::getName)
                    .collect(Collectors.toList());

            Map<String, SelectStatement> selectStatements = this.queryTemplateDao.findByNames(commonTableExpressionNames);

            commonTableExpressions.forEach(commonTableExpression -> {
                // Set each Common Table Expression's SelectStatement.
                SelectStatement selectStatement = selectStatements.get(commonTableExpression.getName());
                selectStatement.setCriteriaArguments(commonTableExpression.getParametersAndArguments());
                commonTableExpression.setSelectStatement(selectStatement);
            });
        }
    }

    @Override
    public List<Integer> getVersions(String name) {
        return this.queryTemplateDao.getVersions(name);
    }

    @Override
    public SelectStatement.Metadata getMetadata(String name, int version) {
        return this.queryTemplateDao.getMetadata(name, version);
    }

    /**
     * A private helper method for setting the {@param selectStatement}'s {@link List<CriterionParameter>}.
     *
     * @param selectStatement {@link SelectStatement}
     */
    private void setSelectStatementCriteriaParameters(SelectStatement selectStatement) {
        selectStatement.getCriteria().forEach(criterion -> {
            // Check that the column exists.
            if (this.databaseMetadataCacheDao.columnExists(criterion.getColumn())) {
                // If so, create a CriterionParameter for each parameter and add it to the SelectStatement's Metadata.
                criterion.getFilter().getParameters().forEach(parameter -> {
                    boolean allowsMultipleValues = criterion.getOperator().equals(Operator.in) || criterion.getOperator().equals(Operator.notIn);

                    selectStatement.getMetadata().getCriteriaParameters().add(
                            new CriterionParameter(parameter, criterion.getColumn(), allowsMultipleValues)
                    );
                });
            } else {
                throw new CacheMissException("Did not recognize column, " + criterion.getColumn().getColumnName());
            }
        });
    }

}
