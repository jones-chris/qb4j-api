package net.querybuilder4j.sql.statement.criterion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.querybuilder4j.sql.statement.column.Column;

import java.sql.Types;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CriterionParameter {

    /**
     * The name of the criteria parameter.  For example, "@year" would be "year".
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /**
     * The column of the criterion.
     */
    @JsonProperty(value = "column", required = true)
    private Column column;

    /**
     * Whether the parameter can accept multiple values.
     * NOTE:  This field is set by the API logic based on the database metadata cache, not by the client.
     */
    @JsonProperty(value = "allowsMultipleValues")
    private boolean allowsMultipleValues;

}
