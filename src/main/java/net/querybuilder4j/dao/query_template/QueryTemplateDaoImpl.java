package net.querybuilder4j.dao.query_template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.exceptions.JsonDeserializationException;
import net.querybuilder4j.sql.statement.SelectStatement;
import net.querybuilder4j.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class QueryTemplateDaoImpl implements QueryTemplateDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public QueryTemplateDaoImpl(Qb4jConfig qb4jConfig) {
        DataSource dataSource = qb4jConfig.getQueryTemplateDataSource().getDataSource();
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        String json = Utils.serializeToJson(selectStatement);

        int numRowsInserted = this.jdbcTemplate.update(
                "INSERT INTO query_templates (name, version, query_json, discoverable, created_by, last_updated_by) " +
                     "VALUES (:name, :version, :json, :discoverable, :createdBy, :lastUpdatedBy)",
                Map.of(
                        "name", selectStatement.getName(),
                        "version", selectStatement.getVersion(),
                        "json", json,
                        "discoverable", (selectStatement.isDiscoverable()) ? 1 : 0,
                        "createdBy", selectStatement.getAuthor(),
                        "lastUpdatedBy", selectStatement.getAuthor()
                )
        );

        return numRowsInserted > 0;
    }

    @Override
    public List<String> listNames() {
        return this.jdbcTemplate.queryForList(
                "SELECT DISTINCT name FROM query_templates ORDER BY name ASC",
                Map.of(),
                String.class
        );
    }

    @Override
    public Optional<Integer> getNewestVersion(String name) {
        try {
            return Optional.ofNullable(
                    this.jdbcTemplate.queryForObject(
                            "SELECT version FROM query_templates WHERE name = :name ORDER BY last_updated_ts DESC LIMIT 1",
                            Map.of("name", name),
                            Integer.class
                    )
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public SelectStatement findByName(String name) {
        Objects.requireNonNull(name, "name is null");

        String json = this.jdbcTemplate.queryForObject(
                "SELECT query_json FROM query_templates WHERE name = :name",
                Map.of("name", name),
                String.class
        );

        Objects.requireNonNull(json, "Did not find JSON string for query template with name, " + name);

        try {
            return this.objectMapper.readValue(json, SelectStatement.class);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Could not deserialize JSON string for query template, " + name);
        }
    }

    @Override
    public Map<String, SelectStatement> findByNames(List<String> names) {
        // Check that the names parameter is not null or empty.
        Objects.requireNonNull(names, "names is null");
        if (names.isEmpty()) {
            throw new IllegalArgumentException("names is empty");
        }

        // Create a parameter map and SQL WHERE clause.
        Map<String, String> params = new HashMap<>();
        for (int i=0; i<names.size(); i++) {
            params.put(":name" + i, names.get(i));
        }
        String sqlWhereClause = "WHERE name IN (" + String.join(", ", params.keySet()) + ") ";

        // Query the database.
        Map<String, Object> retrievedObjects = this.jdbcTemplate.queryForMap(
                "SELECT query_json FROM query_templates " + sqlWhereClause,
                params
        );

        // Convert each object to a SelectStatement and return a map with the keys being the query template names and the
        // values being the the query template's SelectStatement.
        Map<String, SelectStatement> selectStatements = new HashMap<>();
        for (Map.Entry<String, Object> entry : retrievedObjects.entrySet()) {
            try {
                SelectStatement selectStatement = this.objectMapper.readValue(entry.getValue().toString(), SelectStatement.class);
                selectStatements.put(entry.getKey(), selectStatement);
            } catch (JsonProcessingException e) {
                throw new JsonDeserializationException("Could not deserialize JSON for query template, " + entry.getKey());
            }
        }

        return selectStatements;
    }

}
