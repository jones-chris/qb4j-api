package net.querybuilder4j.service.query_template;

import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;

import java.util.List;
import java.util.Map;

public interface QueryTemplateService {

    boolean save(SelectStatement selectStatement);
    Map<String, SelectStatement> findByNames(List<String> names);
    SelectStatement findByName(String name, int version);
    List<String> getNames();
    void getCommonTableExpressionSelectStatement(List<CommonTableExpression> commonTableExpressions);
    List<Integer> getVersions(String name);
    SelectStatement.Metadata getMetadata(String name, int version);

}
