package net.querybuilder4j.controller.database.metadata;

import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.schema.Schema;
import net.querybuilder4j.sql.statement.table.Table;
import net.querybuilder4j.service.database.metadata.DatabaseMetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://querybuilder4j.net" })
@RequestMapping("/metadata")
public class DatabaseMetadataController {

    private DatabaseMetaDataService databaseMetaDataService;

    @Autowired
    public DatabaseMetadataController(DatabaseMetaDataService databaseMetaDataService) {
        this.databaseMetaDataService = databaseMetaDataService;
    }

    /**
     * Returns all QueryBuilder4J target data sources.
     *
     * @return A ResponseEntity containing a List of Database objects.
     */
    @GetMapping(value = "/database")
    public ResponseEntity<Set<Database>> getDatabases() {
        Set<Database> databases = this.databaseMetaDataService.getDatabases();
        return ResponseEntity.ok(databases);
    }

    /**
     * Returns all database schemas that a given user has access to.
     *
     * @return A ResponseEntity containing a List of Schema objects.
     */
    @GetMapping(value = "/{database}/schema")
    public ResponseEntity<List<Schema>> getSchemas(@PathVariable String database) throws Exception {
        List<Schema> schemas = databaseMetaDataService.getSchemas(database);
        return ResponseEntity.ok(schemas);
    }

    /**
     * Returns all database tables and views that a given user has access to.
     *
     * @param database The database of the tables and views to retrieve.
     * @param schemas The schemas of the tables and views to retrieve.
     * @return A ResponseEntity containing a List of Table objects.
     */
    @GetMapping(value = "/{database}/{schemas}/table-and-view")
    public ResponseEntity<List<Table>> getTablesAndViews(@PathVariable String database,
                                                         @PathVariable String schemas) throws Exception {
        String[] splitSchemas = schemas.split("&");
        List<Table> allTables = new ArrayList<>();
        for (String schema : splitSchemas) {
            List<Table> tables = databaseMetaDataService.getTablesAndViews(database, schema);
            allTables.addAll(tables);
        }

        return ResponseEntity.ok(allTables);
    }

    /**
     * Returns all columns for any number of tables or views given a schema name and table/view name (user permissions apply).
     *
     * @param tables A List of Table objects for which to retrieve columns
     * @return A ResponseEntity containing a List of Column objects.
     */
    @PostMapping(value = "/{database}/{schema}/{tables}/column")
    public ResponseEntity<List<Column>> getColumns(@RequestBody List<Table> tables) throws Exception {
        List<Column> allColumns = new ArrayList<>();
        for (Table table : tables) {
            List<Column> columns = databaseMetaDataService.getColumns(table.getDatabaseName(), table.getSchemaName(), table.getTableName());
            allColumns.addAll(columns);
        }

        return ResponseEntity.ok(allColumns);
    }

}
