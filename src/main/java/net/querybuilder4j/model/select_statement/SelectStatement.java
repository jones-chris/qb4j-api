package net.querybuilder4j.model.select_statement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import net.querybuilder4j.converter.CriteriaDeserializer;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Join;
import net.querybuilder4j.model.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SelectStatement {

    /**
     * The name of the SelectStatement.
     */
    private String name = "";

    /**
     * The database metadata object that can be used to find the database that the SelectStatement is intended to be
     * executed against.
     */
    private Database database;

    /**
     * The columns in the SELECT SQL clause.
     */
    private List<Column> columns = new ArrayList<>();

    /**
     * The table in the FROM SQL clause.
     */
    private Table table;

    /**
     * The criteria in the WHERE SQL clause.
     */
    @JsonDeserialize(using = CriteriaDeserializer.class)
    private List<Criterion> criteria = new ArrayList<>();

    /**
     * The joins in the JOIN SQL clause.
     */
    private List<Join> joins = new ArrayList<>();

    /**
     * Whether to include DISTINCT in the SELECT SQL clause
     */
    private boolean distinct;

    /**
     * Whether to build the GROUP BY SQL clause.
     */
    private boolean groupBy;

    /**
     * Whether to build the ORDER BY SQL clause.
     */
    private boolean orderBy;

    /**
     * Whether to order by ascending or descending in the ORDER BY SQL clause.
     */
    private boolean ascending;

    /**
     * The limit in the LIMITS SQL clause.
     */
    private Long limit = null;

    /**
     * The offset in the OFFSET SQL clause.
     */
    private Long offset = null;

    /**
     * Whether to add criteria to the WHERE SQL clause to exclude records that would have NULL values for all columns
     * in the SELECT SQL clause.
     */
    private boolean suppressNulls;

    /**
     * The sub queries.  The key is the sub query name to find in the SelectStatement's criteria's filter and the value is the sub query SQL string representation.
     */
    private Map<String, String> subQueries = new HashMap<>();

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    private Map<String, String> criteriaArguments = new HashMap<>();

    /**
     * The query's criteria parameters.  The key is the name of the parameter to find in the SelectStatement's criteria.
     * The value is a description of the parameter that users choose in the front end app's UI.
     */
    private List<CriterionParameter> criteriaParameters = new ArrayList<>();

}
