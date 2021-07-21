package net.querybuilder4j.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.querybuilder4j.exceptions.JsonSerializationException;
import net.querybuilder4j.sql.statement.criterion.CriteriaTreeFlattener;
import net.querybuilder4j.sql.statement.criterion.Criterion;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CriteriaSerializer extends StdSerializer<List<Criterion>> {

    public CriteriaSerializer() {
        this(null);
    }

    protected CriteriaSerializer(Class<List<Criterion>> t) {
        super(t);
    }

    @Override
    public void serialize(
            List<Criterion> criterionList,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        Map<Integer, List<Criterion>> flattenedCriteria = CriteriaTreeFlattener.flattenCriteria(criterionList, new HashMap<>());

        jsonGenerator.writeStartArray();

        // Flatten criteria values, which is are all List<Criterion>.
        List<Criterion> flattenedCriteriaValues = flattenedCriteria.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Write each criterion to the jsonGenerator.
        flattenedCriteriaValues.forEach(criterion -> {
            try {
                jsonGenerator.writeStartObject();

                // Id
                jsonGenerator.writeNumberField("id", criterion.getId());

                // Parent Id
                Criterion parentCriterion = criterion.getParentCriterion();
                if (parentCriterion != null) {
                    jsonGenerator.writeNumberField("parentId", parentCriterion.getId());
                }

                // Conjunction
                jsonGenerator.writeStringField("conjunction", criterion.getConjunction().toString());

                // Column
                jsonGenerator.writeObjectField("column", criterion.getColumn());

                // Operator
                jsonGenerator.writeStringField("operator", criterion.getOperator().toString());

                // Filter object with Values array
                jsonGenerator.writeObjectFieldStart("filter");
                jsonGenerator.writeArrayFieldStart("values");
                for (String filterValue : criterion.getFilter().getValues()) {
                    jsonGenerator.writeString(filterValue);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();

                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                throw new JsonSerializationException(e);
            }
        });

        jsonGenerator.writeEndArray();
    }

}