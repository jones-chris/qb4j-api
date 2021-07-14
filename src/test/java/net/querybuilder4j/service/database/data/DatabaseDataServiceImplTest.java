package net.querybuilder4j.service.database.data;

import net.querybuilder4j.dao.database.data.DatabaseDataDao;
import net.querybuilder4j.util.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseDataServiceImplTest {

    @Spy
    private DatabaseDataDao databaseDataDao;

    @InjectMocks
    private DatabaseDataServiceImpl databaseDataService;

    @Test
    public void executeQuery_callsDaoMethodAndPassesResultBack() {
        QueryResult expectedQueryResult = mock(QueryResult.class);
        when(this.databaseDataDao.executeQuery(anyString(), anyString()))
                .thenReturn(expectedQueryResult);

        QueryResult actualQueryResult = this.databaseDataService.executeQuery("database", "SELECT * FROM table");

        verify(this.databaseDataDao, times(1))
                .executeQuery(anyString(), anyString());
        assertEquals(expectedQueryResult, actualQueryResult);
    }

    @Test
    public void getColumnMembers_callsDaoMethodAndPassesResultBack() {
        QueryResult expectedQueryResult = mock(QueryResult.class);
        when(this.databaseDataDao.getColumnMembers(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(expectedQueryResult);

        QueryResult actualQueryResult = this.databaseDataService.getColumnMembers(
                "database",
                "schema",
                "table",
                "column",
                10,
                0,
                true,
                "%search%"
        );

        verify(this.databaseDataDao, times(1))
                .getColumnMembers(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), anyString());
        assertEquals(expectedQueryResult, actualQueryResult);
    }

}