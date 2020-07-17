package net.querybuilder4j.service.database.data;

import net.querybuilder4j.dao.database.data.DatabaseDataDao;
import net.querybuilder4j.model.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDataServiceImpl implements DatabaseDataService {

    private DatabaseDataDao databaseDataDao;

    @Autowired
    public DatabaseDataServiceImpl(DatabaseDataDao databaseDataDao) {
        this.databaseDataDao = databaseDataDao;
    }

    @Override
    public QueryResult executeQuery(String databaseName, String sql) throws Exception {
        return databaseDataDao.executeQuery(databaseName, sql);
    }

    @Override
    public QueryResult getColumnMembers(String databaseName,
                                        String schema,
                                        String table,
                                        String column,
                                        int limit,
                                        int offset,
                                        boolean ascending,
                                        String search) throws Exception {
        return databaseDataDao.getColumnMembers(databaseName, schema, table, column, limit, offset, ascending, search);
    }

}
