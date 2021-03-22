package net.querybuilder4j.sql.statement.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import net.querybuilder4j.sql.statement.table.Table;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Schema {

    private String fullyQualifiedName;

    private String databaseName;

    private String schemaName;

    @JsonIgnore
    private List<Table> tables = new ArrayList<>();

    public Schema(String databaseName, String schemaName) {
        this.fullyQualifiedName = String.format("%s.%s", databaseName, schemaName);
        this.databaseName = databaseName;
        this.schemaName = schemaName;
    }

}
