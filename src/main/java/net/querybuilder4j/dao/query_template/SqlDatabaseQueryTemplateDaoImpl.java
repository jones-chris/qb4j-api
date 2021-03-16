package net.querybuilder4j.dao.query_template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.exceptions.JsonDeserializationException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SqlDatabaseQueryTemplateDaoImpl implements QueryTemplateDao {

    private final DatabaseType databaseType;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SqlDatabaseQueryTemplateDaoImpl(Qb4jConfig qb4jConfig) {
        this.databaseType = qb4jConfig.getQueryTemplateDataSource().getDatabaseType();

        DataSource dataSource = qb4jConfig.getQueryTemplateDataSource().getDataSource();
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        String json = Utils.serializeToJson(selectStatement);

        String saveQuery = this.getSaveSqlQuery();
        Objects.requireNonNull(saveQuery, "saveQuery is null");

        int numRowsInserted = this.jdbcTemplate.update(
                saveQuery,
                Map.of(
                        "name", selectStatement.getMetadata().getName(),
                        "version", selectStatement.getMetadata().getVersion(),
                        "json", json,
                        "discoverable", selectStatement.getMetadata().isDiscoverable(),
                        "createdBy", selectStatement.getMetadata().getAuthor(),
                        "lastUpdatedBy", selectStatement.getMetadata().getAuthor()
                )
        );

        return numRowsInserted > 0;
    }

    @Override
    public List<String> listNames() {
        return this.jdbcTemplate.queryForList(
                this.getListNamesSqlQuery(),
                Map.of(),
                String.class
        );
    }

    @Override
    public Optional<Integer> getNewestVersion(String name) {
        try {
            return Optional.ofNullable(
                    this.jdbcTemplate.queryForObject(
                            this.getNewestVersionSqlQuery(),
                            Map.of("name", name),
                            Integer.class
                    )
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Integer> getVersions(String name) {
        Objects.requireNonNull(name, "name is null");

        return this.jdbcTemplate.queryForList(
                this.getVersionsSqlQuery(),
                Map.of("name", name),
                Integer.class
        );
    }

    @Override
    public SelectStatement.Metadata getMetadata(String name, int version) {
        Objects.requireNonNull(name, "name is null");

        String metadataQuery = this.getMetadataSqlQuery();
        Objects.requireNonNull(metadataQuery, "metadataQuery is null");

        return this.jdbcTemplate.queryForObject(
                metadataQuery,
                Map.of(
                        "name", name,
                        "version", version
                ),
                (resultSet, i) -> {
                    String metadataJson = resultSet.getString(1);
                    try {
                        return this.objectMapper.readValue(metadataJson, SelectStatement.Metadata.class);
                    } catch (JsonProcessingException e) {
                        throw new JsonDeserializationException("Could not deserialize " + name + " and version " + version);
                    }
                }
        );
    }

    @Override
    public SelectStatement findByName(String name, int version) {
        Objects.requireNonNull(name, "name is null");

        String json = this.jdbcTemplate.queryForObject(
                this.getFindByNameSqlQuery(),
                Map.of(
                        "name", name,
                        "version", version
                ),
                String.class
        );

        Objects.requireNonNull(json, "Did not find JSON string for query template with name, " + name);

        try {
            return this.objectMapper.readValue(json, SelectStatement.class);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Could not deserialize JSON string for query template, " + name);
        }
    }

    // todo:  add this method back after producing an MVP.
//    @Override
//    public Map<String, SelectStatement> findByNames(List<String> names) {
//        // Check that the names parameter is not null or empty.
//        Objects.requireNonNull(names, "names is null");
//        if (names.isEmpty()) {
//            throw new IllegalArgumentException("names is empty");
//        }
//
//        // Create a parameter map and SQL WHERE clause.
//        Map<String, String> params = new HashMap<>();
//        for (int i=0; i<names.size(); i++) {
//            params.put("name" + i, names.get(i));
//        }
//        String sqlWhereClause = "WHERE name IN (" + params.keySet().stream().map(key -> ":" + key).collect(Collectors.joining(", ")) + ") ";
//
//        // Query the database.
//        String beginningQueryFragment = this.getFindByNamesSqlQueryStartingFragment();
//        Map<String, Object> retrievedObjects = this.jdbcTemplate.query(
//                beginningQueryFragment + sqlWhereClause,
//                params,
//                (resultSet, i) -> {
//                    System.out.println(i);
//                }
//        );
//
//        // Convert each object to a SelectStatement and return a map with the keys being the query template names and the
//        // values being the the query template's SelectStatement.
//        Map<String, SelectStatement> selectStatements = new HashMap<>();
//        for (Map.Entry<String, Object> entry : retrievedObjects.entrySet()) {
//            try {
//                SelectStatement selectStatement = this.objectMapper.readValue(entry.getValue().toString(), SelectStatement.class);
//                selectStatements.put(entry.getKey(), selectStatement);
//            } catch (JsonProcessingException e) {
//                throw new JsonDeserializationException("Could not deserialize JSON for query template, " + entry.getKey());
//            }
//        }
//
//        return selectStatements;
//    }

    private String getSaveSqlQuery() {
        switch (this.databaseType) {
            case PostgreSQL:
                return "INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by) " +
                        "VALUES (:name, :version, :json, :discoverable, :createdBy, :lastUpdatedBy)";
            case MySql:
                return null;
            case SqlServer:
                return null;
            case Oracle:
                return null;
            default:
                throw new IllegalArgumentException("No save SQL query for database type, " + this.databaseType);
        }
    }

    private String getListNamesSqlQuery() {
        if (this.databaseType.equals(DatabaseType.PostgreSQL) ||
                this.databaseType.equals(DatabaseType.MySql) ||
                this.databaseType.equals(DatabaseType.Oracle) ||
                this.databaseType.equals(DatabaseType.SqlServer)) {
            return "SELECT DISTINCT name FROM qb4j.query_templates ORDER BY name ASC";
        } else {
            throw new IllegalArgumentException("No listNames SQL query for database type, " + this.databaseType);
        }
    }

    private String getNewestVersionSqlQuery() {
        if (this.databaseType.equals(DatabaseType.PostgreSQL) ||
                this.databaseType.equals(DatabaseType.MySql) ||
                this.databaseType.equals(DatabaseType.Oracle) ||
                this.databaseType.equals(DatabaseType.SqlServer)) {
            return "SELECT version FROM qb4j.query_templates WHERE name = :name ORDER BY last_updated_ts DESC LIMIT 1";
        } else {
            throw new IllegalArgumentException("No listNames SQL query for database type, " + this.databaseType);
        }
    }

    private String getVersionsSqlQuery() {
        if (this.databaseType.equals(DatabaseType.PostgreSQL) ||
                this.databaseType.equals(DatabaseType.MySql) ||
                this.databaseType.equals(DatabaseType.Oracle) ||
                this.databaseType.equals(DatabaseType.SqlServer)) {
            return "SELECT version FROM qb4j.query_templates WHERE name = :name";
        } else {
            throw new IllegalArgumentException("No getVersions SQL query for database type, " + this.databaseType);
        }
    }

    private String getMetadataSqlQuery() {
        switch (this.databaseType) {
            case PostgreSQL:
                return "SELECT query_json::json#>>'{metadata}' FROM qb4j.query_templates WHERE name = :name AND version = :version";
            case MySql:
                return null;
            case Oracle:
                return null;
            case SqlServer:
                return null;
            default:
                throw new IllegalArgumentException("No getMetadata SQL query for database type, " + this.databaseType);
        }
    }

    private String getFindByNameSqlQuery() {
        if (this.databaseType.equals(DatabaseType.PostgreSQL) ||
                this.databaseType.equals(DatabaseType.MySql) ||
                this.databaseType.equals(DatabaseType.Oracle) ||
                this.databaseType.equals(DatabaseType.SqlServer)) {
            return "SELECT query_json FROM qb4j.query_templates WHERE name = :name AND version = :version";
        } else {
            throw new IllegalArgumentException("No findByName SQL query for database type, " + this.databaseType);
        }
    }

    private String getFindByNamesSqlQueryStartingFragment() {
        if (this.databaseType.equals(DatabaseType.PostgreSQL) ||
                this.databaseType.equals(DatabaseType.MySql) ||
                this.databaseType.equals(DatabaseType.Oracle) ||
                this.databaseType.equals(DatabaseType.SqlServer)) {
            return "SELECT name, query_json FROM qb4j.query_templates ";
        } else {
            throw new IllegalArgumentException("No findByNames SQL query fragment for database type, " + this.databaseType);
        }
    }
}
