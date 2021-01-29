package net.querybuilder4j.sql.statement.criterion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
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
     * The column of the criteria in table.column format.  For example, "employees.first_name".
     */
    @JsonProperty(value = "column", required = true)
    private String column;

    /**
     * The description of the parameter.
     */
    @JsonProperty(value = "description", required = true)
    private String description;

}
