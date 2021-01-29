package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.sql.statement.validator.DatabaseMetadataCacheValidator;
import net.querybuilder4j.service.query_template.QueryTemplateService;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                               DatabaseMetadataCacheValidator databaseMetadataCacheValidator,
                               QueryTemplateService queryTemplateService) {
        super(databaseMetadataCache, databaseMetadataCacheValidator, queryTemplateService);
        beginningDelimiter = '[';
        endingDelimiter = ']';
    }

    @Override
    public String buildSql() {
        this.createCommonTableExpressionClause();
        this.createSelectClause();
        this.createFromClause();
        this.createWhereClause();
        this.createGroupByClause();
        this.createOrderByClause();
        this.createOffsetClause();
        this.createLimitClause();

        return this.stringBuilder.toString();
    }

    @Override
    protected void createOffsetClause() {
        Long offset = this.selectStatement.getOffset();

        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

    @Override
    protected void createLimitClause() {
        Long limit = this.selectStatement.getLimit();

        if (limit != null) {
            this.stringBuilder.append(" FETCH NEXT ").append(limit).append(" ROWS ONLY ");
        }
    }

}
