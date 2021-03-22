package net.querybuilder4j.controller.query_template;

import net.querybuilder4j.service.query_template.QueryTemplateService;
import net.querybuilder4j.sql.statement.SelectStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @GetMapping("/{name}")
    public ResponseEntity<SelectStatement> getQueryTemplateById(@PathVariable String name,
                                                                @RequestParam int version) {
        SelectStatement queryTemplate = queryTemplateService.findByName(name, version);
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
        if (selectStatement.getMetadata() == null ||
                selectStatement.getMetadata().getName() == null ||
                selectStatement.getMetadata().getName().isEmpty()) {
            throw new IllegalStateException("The name of the select statement cannot be null or an empty string when saving it");
        }

        this.queryTemplateService.save(selectStatement);

        // todo:  add a HATEOAS link with the query name and version here.

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{name}/versions")
    public ResponseEntity<List<Integer>> getQueryTemplateVersions(@PathVariable String name) {
        List<Integer> versions = this.queryTemplateService.getVersions(name);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("{name}/metadata")
    public ResponseEntity<SelectStatement.Metadata> getQueryTemplateMetadata(@PathVariable String name,
                                                                             @RequestParam int version) {
        SelectStatement.Metadata metadata = this.queryTemplateService.getMetadata(name, version);
        return ResponseEntity.ok(metadata);
    }

}
