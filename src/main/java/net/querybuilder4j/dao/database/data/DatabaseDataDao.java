package net.querybuilder4j.dao.database.data;

import net.querybuilder4j.model.QueryResult;

public interface DatabaseDataDao {

    QueryResult executeQuery(String databaseName, String sql) throws Exception;
    QueryResult getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset, boolean ascending, String search) throws Exception;

}
