package net.querybuilder4j.service.query_template;

import net.querybuilder4j.model.select_statement.SelectStatement;

import java.util.List;

public interface QueryTemplateService {

    boolean save(String primaryKey, String json);
    SelectStatement findByName(String name);
    List<String> getNames(Integer limit, Integer offset, boolean ascending) throws Exception;

}
