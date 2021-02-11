package net.querybuilder4j.controller.query_template;

import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Get query template names.
     *
     * @return A {@link ResponseEntity} containing a {@link List<String>} with each {@link String} being the name of a
     * query template.
     */
    @GetMapping
    public ResponseEntity<List<String>> getQueryTemplates() {
        List<String> queryTemplateNames = queryTemplateService.getNames();
        return ResponseEntity.ok(queryTemplateNames);
    }

    /**
     * Get a query template by its unique name.
     *
     * @param name The name of the query template to retrieve.
     * @return The {@link SelectStatement} object with the name parameter.
     */
    @GetMapping(value = "/{name}")
    public ResponseEntity<SelectStatement> getQueryTemplateById(@PathVariable String name) {
        SelectStatement queryTemplate = queryTemplateService.findByName(name);
        return ResponseEntity.ok(queryTemplate);
    }

    /**
     * Save a {@link SelectStatement} object.
     *
     * @param selectStatement The {@link SelectStatement} object to save.
     * @return A ResponseEntity object.
     */
    @PostMapping
    public ResponseEntity<?> saveQueryTemplate(@RequestBody SelectStatement selectStatement) {
        if (selectStatement.getName() == null) {
            throw new IllegalStateException("The name of the select statement cannot be null when saving it");
        }

        this.queryTemplateService.save(selectStatement);

        // todo:  add a link with the query name ({query_name}_{version}) here.

        return ResponseEntity.ok().build();
    }

}
