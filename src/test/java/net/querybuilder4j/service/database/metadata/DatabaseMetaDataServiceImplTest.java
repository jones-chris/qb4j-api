package net.querybuilder4j.service.database.metadata;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheDao;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMetaDataServiceImplTest {

    @Spy
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @InjectMocks
    private DatabaseMetaDataServiceImpl databaseMetaDataService;

    @Test
    public void getDatabases_callsDaoMethodAndPassesResultBack() {
        Set<Database> expectedDatabases = Set.of(new Database());
        when(this.databaseMetadataCacheDao.getDatabases())
                .thenReturn(expectedDatabases);

        Set<Database> resultingDatabases = this.databaseMetaDataService.getDatabases();

        verify(this.databaseMetadataCacheDao, times(1))
                .getDatabases();
        assertEquals(expectedDatabases, resultingDatabases);
    }

    @Test
    public void getSchemas_callsDaoMethodAndPassesResultBack() {
        List<Schema> expectedSchemas = List.of(new Schema());
        when(this.databaseMetadataCacheDao.findSchemas(anyString()))
                .thenReturn(expectedSchemas);

        List<Schema> resultingSchemas = this.databaseMetaDataService.getSchemas("schema");

        verify(this.databaseMetadataCacheDao, times(1)).
                findSchemas(anyString());
        assertEquals(expectedSchemas, resultingSchemas);
    }

    @Test
    public void getTablesAndViews_callsDaoMethodAndPassesResultBack() {
        List<Table> expectedTables = List.of(new Table());
        when(this.databaseMetadataCacheDao.findTables(anyString(), anyString()))
                .thenReturn(expectedTables);

        List<Table> resultingTables = this.databaseMetaDataService.getTablesAndViews("database", "schema");

        verify(this.databaseMetadataCacheDao, times(1))
                .findTables(anyString(), anyString());
        assertEquals(expectedTables, resultingTables);
    }

    @Test
    public void getColumns_callsDaoMethodAndPassesResultBack() {
        List<Column> expectedColumns = List.of(new Column());
        when(this.databaseMetadataCacheDao.findColumns(anyString(), anyString(), anyString()))
                .thenReturn(expectedColumns);

        List<Column> resultingColumns = this.databaseMetaDataService.getColumns("database", "schema", "table");

        verify(this.databaseMetadataCacheDao, times(1))
                .findColumns(anyString(), anyString(), anyString());
        assertEquals(expectedColumns, resultingColumns);
    }

}