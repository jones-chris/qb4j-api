package net.querybuilder4j.sql.builder;

import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.exceptions.DatabaseTypeNotRecognizedException;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.validator.DatabaseMetadataCacheValidator;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SqlBuilderFactory {

    private DatabaseMetadataCache databaseMetadataCache;

    private DatabaseMetadataCacheValidator databaseMetadataCacheValidator;

    private QueryTemplateService queryTemplateService;

    @Autowired
    public SqlBuilderFactory(DatabaseMetadataCache databaseMetadataCache, DatabaseMetadataCacheValidator databaseMetadataCacheValidator,
                             QueryTemplateService queryTemplateService) {
        this.databaseMetadataCache = databaseMetadataCache;
        this.databaseMetadataCacheValidator = databaseMetadataCacheValidator;
        this.queryTemplateService = queryTemplateService;
    }

    public SqlBuilder buildSqlBuilder(SelectStatement selectStatement) {
        SqlBuilder sqlBuilder;

        // Get the database type from the cache rather than trusting what the client sends us.
        String databaseName = selectStatement.getDatabase().getDatabaseName();
        Optional<Database> database = Optional.ofNullable(this.databaseMetadataCache.findDatabases(databaseName));
        DatabaseType databaseType;
        if (database.isPresent()) {
            databaseType = database.get().getDatabaseType();
        } else {
            throw new CacheMissException("Database not found, " + databaseName);
        }

        switch (databaseType) {
            case MySql:
                sqlBuilder = new MySqlSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case Oracle:
                sqlBuilder = new OracleSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case PostgreSQL:
                sqlBuilder = new PostgresSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case SqlServer:
                sqlBuilder = new SqlServerSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case Sqlite:
                sqlBuilder = new SqliteSqlBuilder(this.databaseMetadataCache, this.databaseMetadataCacheValidator, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            default:
                throw new DatabaseTypeNotRecognizedException(databaseType);
        }

        return sqlBuilder;
    }

}
