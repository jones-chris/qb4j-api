package net.querybuilder4j.dao.database.data;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.Filter;
import net.querybuilder4j.sql.statement.criterion.Operator;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.table.Table;
import net.querybuilder4j.util.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Repository
public class DatabaseDataDaoImpl implements DatabaseDataDao {

    private QbConfig qbConfig;

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    private SqlBuilderFactory sqlBuilderFactory;

    @Autowired
    public DatabaseDataDaoImpl(
            QbConfig qbConfig,
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            SqlBuilderFactory sqlBuilderFactory
    ) {
        this.qbConfig = qbConfig;
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.sqlBuilderFactory = sqlBuilderFactory;
    }

    @Override
    public QueryResult executeQuery(String databaseName, String sql) throws Exception {
        DataSource dataSource = qbConfig.getTargetDataSourceAsDataSource(databaseName);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return new QueryResult(rs, sql);
        }

    }

    @Override
    public QueryResult getColumnMembers(String databaseName, String schemaName, String tableName, String columnName, int limit, int offset,
                                        boolean ascending, String search) throws Exception {
        SelectStatement selectStatement = new SelectStatement();
        DatabaseType databaseType = this.databaseMetadataCacheDao.findDatabases(databaseName).getDatabaseType();
        selectStatement.setDatabase(new Database(databaseName, databaseType));
        selectStatement.setDistinct(true);
        int columnDataType = this.databaseMetadataCacheDao.findColumnByName(databaseName, schemaName, tableName, columnName)
                .getDataType();
        Column column = new Column(databaseName, schemaName, tableName, columnName, columnDataType, null);
        selectStatement.getColumns().add(column);
        selectStatement.setTable(new Table(databaseName, schemaName, tableName));
        if (search != null) {
            Filter filter = new Filter();
            filter.getValues().add(search);

            Criterion criterion = new Criterion(0, null, null, column, Operator.like, filter, null);
            selectStatement.getCriteria().add(criterion);
        }
        selectStatement.setLimit(Integer.toUnsignedLong(limit));
        selectStatement.setOffset(Integer.toUnsignedLong(offset));
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(ascending);

        String sql = this.sqlBuilderFactory.buildSqlBuilder(selectStatement)
                .buildSql();

        DataSource dataSource = qbConfig.getTargetDataSourceAsDataSource(databaseName);
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return new QueryResult(rs, null);
        }

    }

}
