package net.querybuilder4j.controller.database.data;

import net.querybuilder4j.service.database.data.DatabaseDataService;
import net.querybuilder4j.sql.builder.SqlBuilder;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.util.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RequestMapping("/data")
public class DatabaseDataController {

    private DatabaseDataService databaseDataService;

    private SqlBuilderFactory sqlBuilderFactory;

    @Autowired
    public DatabaseDataController(DatabaseDataService databaseDataService, SqlBuilderFactory sqlBuilderFactory) {
        this.databaseDataService = databaseDataService;
        this.sqlBuilderFactory = sqlBuilderFactory;
    }

    /**
     * Get a column's members.
     *
     * @param database The database of the column members to retrieve.
     * @param schema The schema of the column members to retrieve.
     * @param table The table of the column members to retrieve.
     * @param column The column of the column members to retrieve.
     * @param limit The maximum number of column members to retrieve (used for pagination).
     * @param offset The column member record number that the results should start at (used for pagination).
     * @param ascending Whether the query that retrieves the column members should be in ascending or descending order.
     * @param search The text that the column members should contain.
     * @return A ResponseEntity containing
     */
    @GetMapping(value = "/{database}/{schema}/{table}/{column}/column-member")
    public ResponseEntity<QueryResult> getColumnMembers(@PathVariable String database,
                                                        @PathVariable String schema,
                                                        @PathVariable String table,
                                                        @PathVariable String column,
                                                        @RequestParam int limit,
                                                        @RequestParam int offset,
                                                        @RequestParam boolean ascending,
                                                        @RequestParam(required = false) String search) throws Exception {
        QueryResult columnMembers = databaseDataService.getColumnMembers(database, schema, table, column, limit, offset, ascending, search);
        return ResponseEntity.ok(columnMembers);
    }

    /**
     * Execute a SelectStatement, audits the database for any unexpected changes, heals the database if necessary, publishes
     * a request to an SNS topic (if the database needed to be healed), and returns the query's results.
     *
     * @param selectStatement The SelectStatement to build a SQL string for.
     * @return A {@link ResponseEntity} containing a {@link QueryResult}.
     */
    @PostMapping(value = "/{database}/query")
    public ResponseEntity<QueryResult> getQueryResults(@PathVariable String database,
                                                       @RequestBody SelectStatement selectStatement) throws Exception {
        SqlBuilder sqlBuilder = this.sqlBuilderFactory.buildSqlBuilder(selectStatement);
        String sql = sqlBuilder.buildSql();

        QueryResult queryResult = databaseDataService.executeQuery(database, sql);

        return ResponseEntity.ok(queryResult);
    }

}
