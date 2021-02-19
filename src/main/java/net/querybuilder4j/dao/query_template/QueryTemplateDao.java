package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.sql.statement.SelectStatement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QueryTemplateDao {

    SelectStatement findByName(String name, int version);
    Map<String, SelectStatement> findByNames(List<String> names);
    boolean save(SelectStatement selectStatement);
    List<String> listNames();
    Optional<Integer> getNewestVersion(String name);
    List<Integer> getVersions(String name);
    SelectStatement.Metadata getMetadata(String name, int version);

}
