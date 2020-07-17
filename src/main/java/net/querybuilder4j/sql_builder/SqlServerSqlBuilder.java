package net.querybuilder4j.sql_builder;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import net.querybuilder4j.model.select_statement.validator.DatabaseMetadataCacheValidator;

public class SqlServerSqlBuilder extends SqlBuilder {

    public SqlServerSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                               DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        super(databaseMetadataCache, databaseMetadataCacheValidator);
        beginningDelimiter = '[';
        endingDelimiter = ']';
    }

    @Override
    public String buildSql() {
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
