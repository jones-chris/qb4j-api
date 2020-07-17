package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.model.select_statement.SelectStatement;

import java.util.List;

public interface QueryTemplateDao {

    SelectStatement findByName(String name);
    boolean save(String primaryKey, String json);
    List<String> listNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
