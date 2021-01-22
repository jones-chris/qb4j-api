package net.querybuilder4j.model.select_statement;

import lombok.*;
import net.querybuilder4j.model.SqlRepresentation;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Filter implements SqlRepresentation {

    private List<String> values = new ArrayList<>();

//    private List<String> subQueries = new ArrayList<>();
//
//    private List<String> parameters = new ArrayList<>();

//    public boolean isEmpty() {
//        return this.values.isEmpty() && this.subQueries.isEmpty() && this.parameters.isEmpty();
//    }
//
//    public boolean hasSubQuery() {
//        return ! this.subQueries.isEmpty();
//    }

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        StringBuilder sql = new StringBuilder();

        if (! this.values.isEmpty()) {
            return sql.append("(").append(String.join(",", this.values)).append(")")
                    .toString();
        } else {
            return "";
        }

    }
}
