package net.querybuilder4j.service.query_template;

import net.querybuilder4j.dao.query_template.QueryTemplateDao;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QueryTemplateServiceImpl implements QueryTemplateService {

    private QueryTemplateDao queryTemplateDao;

    @Autowired
    public QueryTemplateServiceImpl(QueryTemplateDao queryTemplateDao) {
        this.queryTemplateDao = queryTemplateDao;
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        // Get the newest version number in the database.
        this.queryTemplateDao.getNewestVersion(selectStatement.getName())
                .ifPresentOrElse(
                        (currentVersion) -> selectStatement.setVersion(currentVersion + 1), // If version exists, increment by 1.
                        () -> selectStatement.setVersion(0) // If version does not exist, set to 0.
                );

        // Save the Select Statement.
        return queryTemplateDao.save(selectStatement);
    }

    @Override
    public Map<String, SelectStatement> findByNames(List<String> names) {
        return this.queryTemplateDao.findByNames(names);
    }

    @Override
    public SelectStatement findByName(String name) {
        return queryTemplateDao.findByName(name);
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

}
