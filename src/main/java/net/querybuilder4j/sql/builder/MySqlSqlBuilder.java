package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.service.query_template.QueryTemplateService;

public class MySqlSqlBuilder extends SqlBuilder {

    public MySqlSqlBuilder(DatabaseMetadataCacheDao databaseMetadataCacheDao, QueryTemplateService queryTemplateService) {
        super(databaseMetadataCacheDao, queryTemplateService);
        beginningDelimiter = '`';
        endingDelimiter = '`';
    }

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
