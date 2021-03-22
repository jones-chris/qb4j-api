package net.querybuilder4j.service.database.data;

import net.querybuilder4j.util.QueryResult;

public interface DatabaseDataService {

    QueryResult executeQuery(String databaseName, String sql) throws Exception;
    QueryResult getColumnMembers(String databaseName, String schema, String table, String column, int limit, int offset, boolean ascending, String search) throws Exception;

}
