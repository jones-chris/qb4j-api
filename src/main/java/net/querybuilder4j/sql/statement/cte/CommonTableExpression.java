package net.querybuilder4j.sql.statement.cte;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.querybuilder4j.sql.statement.SqlRepresentation;
import net.querybuilder4j.sql.statement.SelectStatement;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class CommonTableExpression implements SqlRepresentation {

    /**
     * The unique identifier of the {@link CommonTableExpression}.
     */
    @JsonProperty(value = "name")
    @NotNull
    @Getter
    @Setter
    @EqualsAndHashCode.Include
    private String name;

    /**
     * The name of the query to retrieve from the query template database.
     */
    @JsonProperty(value = "queryName")
    @NotNull
    @Getter
    @Setter
    private String queryName;

    /**
     * A {@link Map} with the parameters being the keys and the arguments being the values.
     */
    @JsonProperty(value = "parametersAndArguments")
    @Getter
    @Setter
    private Map<String, String> parametersAndArguments = new HashMap<>();

    /**
     * The Common Table Expression's {@link SelectStatement}.
     */
    @JsonIgnore
    @Getter
    @Setter
    private SelectStatement selectStatement;

    /**
     * The {@link CommonTableExpression#selectStatement} built as a SQL string.  This is the SQL that should form the
     * WITH/Common Table Expression clause.
     */
    @JsonIgnore
    @Getter
    @Setter
    private String sql;

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (! this.isBuilt()) {
            throw new IllegalStateException("Common Table Expression's sql is null, a blank string, or an empty string");
        }

        StringBuilder sb = new StringBuilder(" ")
                .append(this.name)
                .append(" AS (")
                .append(this.sql)
                .append(")");

        return sb.toString();
    }

    public boolean isBuilt() {
        return this.sql != null && ! this.sql.trim().equals("");
    }

}
