package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.sql.statement.SelectStatement;

import java.util.List;
import java.util.Map;

public interface QueryTemplateDao {

    SelectStatement findByName(String name);
    Map<String, SelectStatement> findByNames(List<String> names);
    boolean save(String primaryKey, String json);
    List<String> listNames();

}
