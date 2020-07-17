package net.querybuilder4j.sql_builder;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import net.querybuilder4j.model.select_statement.validator.DatabaseMetadataCacheValidator;

public class SqliteSqlBuilder extends SqlBuilder {

    public SqliteSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                            DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        super(databaseMetadataCache, databaseMetadataCacheValidator);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public String buildSql() {
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
