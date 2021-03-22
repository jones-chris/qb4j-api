package net.querybuilder4j.sql.statement.table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.querybuilder4j.sql.builder.SqlValidator;
import net.querybuilder4j.sql.statement.SqlRepresentation;
import net.querybuilder4j.sql.statement.column.Column;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Table implements SqlRepresentation {

    private String fullyQualifiedName;

    private String databaseName;

    private String schemaName;

    private String tableName;

    @JsonIgnore
    private List<Column> columns = new ArrayList<>();

    public Table(String databaseName, String schemaName, String tableName) {
        this.fullyQualifiedName = String.format("%s.%s.%s", databaseName, schemaName, tableName);
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        if (this.schemaName == null || this.schemaName.equals("null")) {
            return String.format(" %s%s%s ",
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter);
        } else {
            return String.format(" %s%s%s.%s%s%s ",
                    beginningDelimiter, SqlValidator.escape(this.schemaName), endingDelimiter,
                    beginningDelimiter, SqlValidator.escape(this.tableName), endingDelimiter);
        }
    }

}
