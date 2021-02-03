package net.querybuilder4j.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import net.querybuilder4j.exceptions.JsonDeserializationException;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.criterion.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CriteriaDeserializer extends StdDeserializer<List<Criterion>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CriteriaDeserializer() {
        this(null);
    }

    protected CriteriaDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<Criterion> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ArrayNode node = jsonParser.getCodec().readTree(jsonParser);
        List<Criterion> deserializedCriteria = new ArrayList<>();
        node.forEach(jsonNode -> {
            Criterion newCriterion = buildCriterion(jsonNode, deserializedCriteria);

            // Only add root criterion directly to the `deserializedCriteria` list.  Non-root/child criterion will be
            // added to the list by being added to non-root/child criterions' child criteria in the `buildCriterion`
            // method.
            if (newCriterion.isRoot()) {
                deserializedCriteria.add(newCriterion);
            }
        });

        return deserializedCriteria;
    }

    private Criterion buildCriterion(JsonNode criterionJson, List<Criterion> deserializedCriteria) {
        Conjunction conjunction = Conjunction.valueOf(criterionJson.get("conjunction").asText());

        Column column = new Column(
                criterionJson.get("column").get("databaseName").asText(),
                criterionJson.get("column").get("schemaName").asText(),
                criterionJson.get("column").get("tableName").asText(),
                criterionJson.get("column").get("columnName").asText(),
                criterionJson.get("column").get("dataType").asInt(),
                criterionJson.get("column").get("alias").asText()
        );
        Operator operator = Operator.valueOf(criterionJson.get("operator").asText());

        Filter filter;
        try {
            filter = this.objectMapper.readValue(criterionJson.get("filter").toString(), Filter.class);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Not able to deserialize criterion filter");
        }

        int id = criterionJson.get("id").asInt();

        // Find parent criterion.
        Criterion parentCriterion = null;
        if (! criterionJson.get("parentId").isNull()) {
            int parentId = criterionJson.get("parentId").asInt();

            List<Criterion> flattenedCriteria = new ArrayList<>();

            CriteriaTreeFlattener.flattenCriteria(deserializedCriteria, new HashMap<>())
                    .forEach((rootIndex, criteria) -> flattenedCriteria.addAll(criteria));

            parentCriterion = flattenedCriteria.stream()
                    .filter(criterion -> criterion.getId() == parentId)
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
        }

        // Instantiate the new criterion.
        Criterion newCriterion = new Criterion(
                id,
                parentCriterion,
                conjunction,
                column,
                operator,
                filter,
                null
        );

        // If the parent criterion is not null, then add the new criterion to it's child criteria.
        if (parentCriterion != null) {
            parentCriterion.getChildCriteria().add(newCriterion);
        }

        return newCriterion;
    }

}