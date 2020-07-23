package net.querybuilder4j.model.select_statement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.querybuilder4j.converter.CriteriaDeserializer;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.Database;
import net.querybuilder4j.model.Join;
import net.querybuilder4j.model.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isGroupBy() {
        return groupBy;
    }

    public void setGroupBy(boolean groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isOrderBy() {
        return orderBy;
    }

    public void setOrderBy(boolean orderBy) {
        this.orderBy = orderBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public void setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
    }

    public Map<String, String> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(Map<String, String> subQueries) {
        this.subQueries = subQueries;
    }

    public Map<String, String> getCriteriaArguments() {
        return criteriaArguments;
    }

    public void setCriteriaArguments(Map<String, String> criteriaArguments) {
        this.criteriaArguments = criteriaArguments;
    }

    public List<CriterionParameter> getCriteriaParameters() {
        return criteriaParameters;
    }

    public void setCriteriaParameters(List<CriterionParameter> criteriaParameters) {
        this.criteriaParameters = criteriaParameters;
    }

    @Override
    public String toString() {
        String s = "";
        try {
            s = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}

        return s;
    }

}
