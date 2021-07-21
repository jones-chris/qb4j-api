package net.querybuilder4j.dao.database.data;

import net.querybuilder4j.util.QueryResult;

public interface DatabaseDataDao {

    QueryResult executeQuery(String databaseName, String sql);
    QueryResult getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset, boolean ascending, String search);

}
