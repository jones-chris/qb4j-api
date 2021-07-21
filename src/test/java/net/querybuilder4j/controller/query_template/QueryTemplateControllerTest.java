package net.querybuilder4j.controller.query_template;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.TestUtils;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(QueryTemplateController.class)
public class QueryTemplateControllerTest {

    @MockBean
    private QueryTemplateService queryTemplateService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getQueryTemplates_returns200() throws Exception {
        List<String> queryTemplateNames = List.of("queryTemplate1", "queryTemplate2");
        when(this.queryTemplateService.getNames())
                .thenReturn(queryTemplateNames);

        this.mockMvc.perform(
                get("/query-template")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(queryTemplateNames)
                )
        );
    }

    @Test
    public void getQueryTemplateById_returns200() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        when(this.queryTemplateService.findByName(anyString(), anyInt()))
                .thenReturn(selectStatement);

        this.mockMvc.perform(
                get("/query-template/name?version=0")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(selectStatement)
                )
        );
    }

    @Test
    public void saveQueryTemplate_returns200() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        selectStatement.getMetadata().setName("bob");
        when(this.queryTemplateService.save(selectStatement))
                .thenReturn(true);

        this.mockMvc.perform(
                post("/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(selectStatement)
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        );
    }

    @Test
    public void saveQueryTemplate_returns400WhenMetadataIsNull() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(null);
        when(this.queryTemplateService.save(selectStatement))
                .thenReturn(true);

        this.mockMvc.perform(
                post("/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(selectStatement)
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(400)
        );
    }

    @Test
    public void getQueryTemplateVersions_returns200() throws Exception {
        List<Integer> versions = List.of(1, 2, 3);
        when(this.queryTemplateService.getVersions(anyString()))
                .thenReturn(versions);

        this.mockMvc.perform(
                get("/query-template/name/versions")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(versions)
                )
        );
    }

    @Test
    public void getQueryTemplateMetadata() throws Exception {
        SelectStatement.Metadata metadata = new SelectStatement.Metadata();
        metadata.setName("bob");
        when(this.queryTemplateService.getMetadata(anyString(), anyInt()))
                .thenReturn(metadata);

        this.mockMvc.perform(
                get("/query-template/name/metadata?version=0")
        ).andDo(
                print()
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(metadata)
                )
        );
    }

}