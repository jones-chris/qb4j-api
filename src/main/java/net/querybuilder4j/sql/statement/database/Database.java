package net.querybuilder4j.sql.statement.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.sql.statement.schema.Schema;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Database {

    @JsonIgnore
    private String fullyQualifiedName;

    private String databaseName;

    private DatabaseType databaseType;

    @JsonIgnore
    private List<Schema> schemas = new ArrayList<>();

    public Database(String databaseName, DatabaseType databaseType) {
        this.fullyQualifiedName = String.format("%s", databaseName);
        this.databaseName = databaseName;
        this.databaseType = databaseType;
    }

}
