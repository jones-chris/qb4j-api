package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.sql.statement.validator.DatabaseMetadataCacheValidator;
import net.querybuilder4j.service.query_template.QueryTemplateService;

public class PostgresSqlBuilder extends SqlBuilder {

    public PostgresSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                              DatabaseMetadataCacheValidator databaseMetadataCacheValidator,
                              QueryTemplateService queryTemplateService) {
        super(databaseMetadataCache, databaseMetadataCacheValidator, queryTemplateService);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public String buildSql() {
        this.createCommonTableExpressionClause();
        this.createSelectClause();
        this.createFromClause();
        this.createJoinClause();
        this.createWhereClause();
        this.createGroupByClause();
        this.createOrderByClause();
        this.createLimitClause();
        this.createOffsetClause();

        return this.stringBuilder.toString();
    }

}
