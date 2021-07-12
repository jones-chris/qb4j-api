package net.querybuilder4j.controller.database.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.TestUtils;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.service.database.metadata.DatabaseMetaDataService;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DatabaseMetadataController.class)
public class DatabaseMetadataControllerTest {

    @MockBean
    private DatabaseMetaDataService databaseMetaDataService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getDatabases_returns200() throws Exception {
        Set<Database> databases = Set.of(new Database());
        when(this.databaseMetaDataService.getDatabases())
                .thenReturn(databases);

        this.mockMvc.perform(
                get("/metadata/database")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(databases)
                )
        );
    }

    @Test
    public void getDatabases_returns500IfCacheMissException() throws Exception {
        when(this.databaseMetaDataService.getDatabases())
                .thenThrow(CacheMissException.class);

        this.mockMvc.perform(
                get("/metadata/database")
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

    @Test
    public void getSchemas_returns200() throws Exception {
        List<Schema> schemas = List.of(new Schema());
        when(this.databaseMetaDataService.getSchemas(anyString()))
                .thenReturn(schemas);

        this.mockMvc.perform(
                get("/metadata/database/schema")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(schemas)
                )
        );
    }

    @Test
    public void getSchemas_returns500IfCacheMissException() throws Exception {
        when(this.databaseMetaDataService.getSchemas(anyString()))
                .thenThrow(CacheMissException.class);

        this.mockMvc.perform(
                get("/metadata/database/schema")
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getTablesAndViews_returns200() throws Exception {
        List<Table> tables1 = List.of(new Table());
        List<Table> tables2 = List.of(new Table());
        List<Table> allTables = new ArrayList<>(tables1);
        allTables.addAll(tables2);
        when(this.databaseMetaDataService.getTablesAndViews(anyString(), anyString()))
                .thenReturn(tables1, tables2);

        this.mockMvc.perform(
                get("/metadata/database/schema1&schema2/table-and-view")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(allTables)
                )
        );
    }

    @Test
    public void getTablesAndViews_returns500IfCacheMissException() throws Exception {
        when(this.databaseMetaDataService.getTablesAndViews(anyString(), anyString()))
                .thenReturn(
                        List.of(
                                new Table()
                        )
                )
                .thenThrow(CacheMissException.class);

        this.mockMvc.perform(
                get("/metadata/database/schema1&schema2/table-and-view")
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getColumns_returns200() throws Exception {
        List<Column> columns1 = List.of(
                TestUtils.buildColumn(Types.VARCHAR)
        );
        List<Column> columns2 = List.of(
                TestUtils.buildColumn(Types.INTEGER)
        );
        List<Column> allColumns = new ArrayList<>(columns1);
        allColumns.addAll(columns2);
        when(this.databaseMetaDataService.getColumns(anyString(), anyString(), anyString()))
                .thenReturn(columns1, columns2);

        this.mockMvc.perform(
                post("/metadata/database/schema/tables/column")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(
                                        List.of(
                                                new Table("database", "schema", "table1"),
                                                new Table("database", "schema", "table2")
                                        )
                                )
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(allColumns)
                )
        );
    }

    @Test
    public void getColumns_returns500IfCacheMissException() throws Exception {
        when(this.databaseMetaDataService.getColumns(anyString(), anyString(), anyString()))
                .thenThrow(CacheMissException.class);

        this.mockMvc.perform(
                post("/metadata/database/schema/tables/column")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(
                                        List.of(
                                                new Table("database", "schema", "table1"),
                                                new Table("database", "schema", "table2")
                                        )
                                )
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

}