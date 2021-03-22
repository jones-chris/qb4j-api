package net.querybuilder4j.sql.builder;

import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.exceptions.DatabaseTypeNotRecognizedException;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SqlBuilderFactory {

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    private QueryTemplateService queryTemplateService;

    @Autowired
    public SqlBuilderFactory(DatabaseMetadataCacheDao databaseMetadataCacheDao, QueryTemplateService queryTemplateService) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.queryTemplateService = queryTemplateService;
    }

    public SqlBuilder buildSqlBuilder(SelectStatement selectStatement) {
        SqlBuilder sqlBuilder;

        // Get the database type from the cache rather than trusting what the client sends us.
        String databaseName = selectStatement.getDatabase().getDatabaseName();
        Optional<Database> database = Optional.ofNullable(this.databaseMetadataCacheDao.findDatabases(databaseName));
        DatabaseType databaseType;
        if (database.isPresent()) {
            databaseType = database.get().getDatabaseType();
        } else {
            throw new CacheMissException("Database not found, " + databaseName);
        }

        switch (databaseType) {
            case MySql:
                sqlBuilder = new MySqlSqlBuilder(this.databaseMetadataCacheDao, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case Oracle:
                sqlBuilder = new OracleSqlBuilder(this.databaseMetadataCacheDao, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case PostgreSQL:
                sqlBuilder = new PostgresSqlBuilder(this.databaseMetadataCacheDao, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case SqlServer:
                sqlBuilder = new SqlServerSqlBuilder(this.databaseMetadataCacheDao, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            case Sqlite:
                sqlBuilder = new SqliteSqlBuilder(this.databaseMetadataCacheDao, this.queryTemplateService)
                        .setStatement(selectStatement);
                break;
            default:
                throw new DatabaseTypeNotRecognizedException(databaseType);
        }

        return sqlBuilder;
    }

}
