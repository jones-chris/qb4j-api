package net.querybuilder4j.sql_builder;

import net.querybuilder4j.cache.DatabaseMetadataCache;
import net.querybuilder4j.model.select_statement.validator.DatabaseMetadataCacheValidator;

public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder(DatabaseMetadataCache databaseMetadataCache,
                            DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        super(databaseMetadataCache, databaseMetadataCacheValidator);
        beginningDelimiter = '"';
        endingDelimiter = '"';
    }

    @Override
    public String buildSql() throws Exception {
        // Select
        createSelectClause();

        // From
        createFromClause();

        // Where
        createWhereClause();

        // Limit
        if (! selectStatement.getCriteria().isEmpty()) {
            this.stringBuilder.append(" AND ");
        } else {
            this.stringBuilder.append(" WHERE ");
        }
        createLimitClause();

        // Group By
        createGroupByClause();

        // Order By
        createOrderByClause();

        // Offset
        createOffsetClause();

        return this.stringBuilder.toString();
    }

    @Override
    protected void createLimitClause() {
        Long limit = this.selectStatement.getLimit();

        if (limit != null) {
            this.stringBuilder.append(" ROWNUM < ").append(limit);
        }
    }

    @Override
    protected void createOffsetClause() {
        Long offset = this.selectStatement.getOffset();

        if (offset != null) {
            this.stringBuilder.append(" OFFSET ").append(offset).append(" ROWS ");
        }
    }

}
