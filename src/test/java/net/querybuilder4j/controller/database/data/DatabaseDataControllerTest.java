package net.querybuilder4j.controller.database.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.TestUtils;
import net.querybuilder4j.exceptions.QueryFailureException;
import net.querybuilder4j.service.database.data.DatabaseDataService;
import net.querybuilder4j.sql.builder.SqlBuilder;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import net.querybuilder4j.util.QueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(DatabaseDataController.class)
public class DatabaseDataControllerTest {

    @MockBean
    private DatabaseDataService databaseDataService;

    @MockBean
    private SqlBuilderFactory sqlBuilderFactory;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getColumnMembers_returns200IfNoSearchQueryParameter() throws Exception {
        ResultSet resultSet = this.buildMockResultSet();
        QueryResult queryResult = new QueryResult(resultSet, "SELECT * FROM table");
        doReturn(queryResult)
                .when(this.databaseDataService)
                .getColumnMembers(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt(),
                        anyInt(),
                        anyBoolean(),
                        any()
                );

        this.mockMvc.perform(
                get("/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                jsonPath("$.sql", notNullValue())
        ).andExpect(
                jsonPath("$.columns", notNullValue())
        ).andExpect(
                jsonPath("$.data", notNullValue())
        ).andExpect(
                jsonPath("$.selectStatement", nullValue())
        );
    }

    @Test
    public void getColumnMembers_returns200IfSearchQueryParameter() throws Exception {
        ResultSet resultSet = this.buildMockResultSet();
        QueryResult queryResult = new QueryResult(resultSet, "SELECT * FROM table");
        doReturn(queryResult)
                .when(this.databaseDataService)
                .getColumnMembers(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt(),
                        anyInt(),
                        anyBoolean(),
                        any()
                );

        this.mockMvc.perform(
                get("/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true&search=mySearchText")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                jsonPath("$.sql", notNullValue())
        ).andExpect(
                jsonPath("$.columns", notNullValue())
        ).andExpect(
                jsonPath("$.data", notNullValue())
        ).andExpect(
                jsonPath("$.selectStatement", nullValue())
        );
    }

    @Test
    public void getColumnMembers_exceptionCauses500ToBeReturned() throws Exception {
        when(this.databaseDataService.getColumnMembers(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenThrow(QueryFailureException.class);

        this.mockMvc.perform(
                get("/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true&search=mySearchText")
        ).andDo(
                print()
        ).andExpect(
                status().is5xxServerError()
        );
    }

    @Test
    public void getQueryResults_returns200IfQueryIsRunSuccessfully() throws Exception {
        SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.buildSql())
                .thenReturn("SELECT * FROM table");
        QueryResult queryResult = new QueryResult(
                this.buildMockResultSet(),
                "SELECT * FROM table"
        );
        when(this.databaseDataService.executeQuery(anyString(), anyString()))
                .thenReturn(queryResult);

        this.mockMvc.perform(
                post("/data/database/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                jsonPath("$.sql", notNullValue())
        ).andExpect(
                jsonPath("$.columns", notNullValue())
        ).andExpect(
                jsonPath("$.data", notNullValue())
        ).andExpect(
                jsonPath("$.selectStatement", notNullValue())
        );
    }

    @Test
    public void getQueryResult_exceptionCauses500ToBeReturned() throws Exception {
        SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.buildSql())
                .thenReturn("SELECT * FROM table");
        when(this.databaseDataService.executeQuery(anyString(), anyString()))
                .thenThrow(QueryFailureException.class);

        this.mockMvc.perform(
                post("/data/database/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is5xxServerError()
        );
    }

    private ResultSet buildMockResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next())
                .thenReturn(true, false);
        when(resultSet.getObject(anyInt()))
                .thenReturn("bob", "joe");

        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData())
                .thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount())
                .thenReturn(2);
        when(resultSetMetaData.getColumnLabel(anyInt()))
                .thenReturn("column1", "column2");

        return resultSet;
    }

}