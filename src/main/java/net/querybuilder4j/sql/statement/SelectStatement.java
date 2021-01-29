package net.querybuilder4j.sql.statement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import net.querybuilder4j.util.CriteriaDeserializer;
import net.querybuilder4j.sql.statement.column.Column;
import net.querybuilder4j.sql.statement.database.Database;
import net.querybuilder4j.sql.statement.criterion.Criterion;
import net.querybuilder4j.sql.statement.criterion.CriterionParameter;
import net.querybuilder4j.sql.statement.join.Join;
import net.querybuilder4j.sql.statement.cte.CommonTableExpression;
import net.querybuilder4j.sql.statement.table.Table;

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
    @JsonProperty(value = "name")
    private String name = "";

    /**
     * The database metadata object that can be used to find the database that the SelectStatement is intended to be
     * executed against.
     */
    @JsonProperty(value = "database", required = true)
    private Database database;

    /**
     * A {@link List< CommonTableExpression >} or also known as WITH statements.
     */
    @JsonProperty(value = "commonTableExpressions")
    private List<CommonTableExpression> commonTableExpressions = new ArrayList<>();

    /**
     * The columns in the SELECT SQL clause.
     */
    @JsonProperty(value = "columns", required = true)
    private List<Column> columns = new ArrayList<>();

    /**
     * The table in the FROM SQL clause.
     */
    @JsonProperty(value = "table", required = true)
    private Table table;

    /**
     * The criteria in the WHERE SQL clause.
     */
    @JsonDeserialize(using = CriteriaDeserializer.class)
    @JsonProperty(value = "criteria")
    private List<Criterion> criteria = new ArrayList<>();

    /**
     * The joins in the JOIN SQL clause.
     */
    @JsonProperty(value = "joins")
    private List<Join> joins = new ArrayList<>();

    /**
     * Whether to include DISTINCT in the SELECT SQL clause
     */
    @JsonProperty(value = "distinct")
    private boolean distinct;

    /**
     * Whether to build the GROUP BY SQL clause.
     */
    @JsonProperty(value = "groupBy")
    private boolean groupBy;

    /**
     * Whether to build the ORDER BY SQL clause.
     */
    @JsonProperty(value = "orderBy")
    private boolean orderBy;

    /**
     * Whether to order by ascending or descending in the ORDER BY SQL clause.
     */
    @JsonProperty(value = "ascending")
    private boolean ascending;

    /**
     * The limit in the LIMITS SQL clause.
     */
    @JsonProperty(value = "limit")
    private Long limit = null;

    /**
     * The offset in the OFFSET SQL clause.
     */
    @JsonProperty(value = "offset")
    private Long offset = null;

    /**
     * Whether to add criteria to the WHERE SQL clause to exclude records that would have NULL values for all columns
     * in the SELECT SQL clause.
     */
    @JsonProperty(value = "suppressNulls")
    private boolean suppressNulls;

    /**
     * The query's criteria runtime arguments.  The key is the name of the parameter to find in the query criteria.  The
     * value is what will be passed into the query criteria.
     */
    @JsonProperty(value = "criteriaArguments")
    private Map<String, String> criteriaArguments = new HashMap<>();

    /**
     * The query's criteria parameters.  The key is the name of the parameter to find in the SelectStatement's criteria.
     * The value is a description of the parameter that users choose in the front end app's UI.
     */
    @JsonProperty(value = "criteriaParameters")
    private List<CriterionParameter> criteriaParameters = new ArrayList<>();

}
