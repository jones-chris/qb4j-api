package net.querybuilder4j.controller.query_template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.service.query_template.QueryTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RequestMapping("/query-template")
public class QueryTemplateController {

    private QueryTemplateService queryTemplateService;

    @Autowired
    public QueryTemplateController(QueryTemplateService queryTemplateService) {
        this.queryTemplateService = queryTemplateService;
    }

    /**
     * Gets query template names.
     *
     * @return A ResponseEntity object containing a List of Strings with each String being the name of a query template.
     */
    @GetMapping(value = "")
    public ResponseEntity<List<String>> getQueryTemplates() {
        List<String> queryTemplateNames = queryTemplateService.getNames();
        return ResponseEntity.ok(queryTemplateNames);
    }

    /**
     * Get a query template by its unique name.
     *
     * @param name The name of the query template to retrieve.
     * @return The SelectStatement object with the name parameter.
     */
    @GetMapping(value = "/{name}")
    public ResponseEntity<SelectStatement> getQueryTemplateById(@PathVariable String name) {
        SelectStatement queryTemplate = queryTemplateService.findByName(name);
        return ResponseEntity.ok(queryTemplate);
    }

    /**
     * Save a SelectStatement object.
     *
     * @param selectStatement The SelectStatement object to save.
     * @return A ResponseEntity object.
     */
    @PostMapping(value = "/")
    public ResponseEntity<?> saveQueryTemplate(@RequestBody SelectStatement selectStatement) {
        Objects.requireNonNull(selectStatement.getName(), "The name of the select statement cannot be null when saving it");

        String json;
        try {
            json = new ObjectMapper().writeValueAsString(selectStatement);
            this.queryTemplateService.save(selectStatement.getName(), json);
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Could not serialize the selectStatement");
        }
    }

}
