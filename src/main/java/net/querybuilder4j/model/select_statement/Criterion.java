package net.querybuilder4j.model.select_statement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.model.Column;
import net.querybuilder4j.model.SqlRepresentation;
import net.querybuilder4j.model.select_statement.validator.Validator;
import net.querybuilder4j.sql_builder.SqlCleanser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static net.querybuilder4j.sql_builder.SqlCleanser.sqlIsClean;

public class Criterion implements SqlRepresentation, Validator {

    private Criterion parentCriterion;
    private Parenthesis openingParenthesis = Parenthesis.Empty;
    private Conjunction conjunction;
    private Column column;
    private Operator operator;
    private String filter;
    private List<Parenthesis> closingParenthesis = new ArrayList<>();
    private List<Criterion> childCriteria = new ArrayList<>();

    public Criterion(Criterion parentCriterion, Conjunction conjunction, Column column, Operator operator, String filter,
                     List<Criterion> childCriteria) {
        this.parentCriterion = parentCriterion;
        this.conjunction = conjunction;
        this.column = column;
        this.operator = operator;
        this.filter = filter;

        if (childCriteria != null) {
            this.childCriteria = childCriteria;
        }
    }

    public Criterion getParentCriterion() {
        return parentCriterion;
    }

    public Parenthesis getOpeningParenthesis() {
        return ofNullable(this.openingParenthesis).orElse(Parenthesis.Empty);
    }

    public Conjunction getConjunction() {
        return ofNullable(this.conjunction).orElse(Conjunction.Empty);
    }

    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    public Column getColumn() {
        return column;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getFilter() {
        return ofNullable(this.filter).orElse("");
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public List<Parenthesis> getClosingParenthesis() {
        return ofNullable(this.closingParenthesis).orElse(Collections.singletonList(Parenthesis.Empty));
    }

    public List<Criterion> getChildCriteria() {
        return childCriteria;
    }

    public void setChildCriteria(List<Criterion> childCriteria) {
        if (childCriteria != null) {
            this.childCriteria = childCriteria;
        }
    }

    public boolean isRoot() {
        return this.parentCriterion == null;
    }

    public List<String> getFilterItems() {
        return Arrays.asList(this.filter.split(","));
    }

    public boolean filterIsEmpty() {
        return this.getFilter().equals("");
    }

    public boolean isParent() {
        return this.childCriteria != null && ! this.childCriteria.isEmpty();
    }

    public boolean hasParent() {
        return this.parentCriterion != null;
    }

    public boolean isLastChildInBranch() {
        return ! this.isParent() && this.hasParent();
    }

    public boolean hasOpeningParenthesis() {
        return this.openingParenthesis.equals(Parenthesis.FrontParenthesis);
    }

    public boolean hasClosingParenthesis() {
        return this.closingParenthesis != null && ! this.closingParenthesis.isEmpty();
    }

    @Override
    public String toString() throws IllegalArgumentException {
        String s = "";
        try {
            s = new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ignored) {}

        return s;
    }

    /**
     * The implementation of the SqlRepresentation interface.  This method returns only this criterion's SQL string
     * representation, not this criterion's childCriteria (if it has any child criteria).  This method does NOT walk the
     * criteria tree.  Use this method only when you want this criterion's SQL string representation and not it's
     * childCriteria.
     *
     * Returns the SQL string representation of the criterion in this format, if the schema were not null:
     * [AND/OR] [FRONT PARENTHESIS] `schema_name`.`table_name`.`column_name` [OPERATOR] filter [END PARENTHESIS]
     *
     * If the schema were null, the SQL string would be:
     * [AND/OR] [FRONT PARENTHESIS] `table_name`.`column_name` [OPERATOR] filter [END PARENTHESIS]
     *
     * @param beginningDelimiter
     * @param endingDelimiter
     * @return
     */
    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        String schema = ofNullable(column.getSchemaName())
                .map(SqlCleanser::escape)
                .orElse(null);

        String table = ofNullable(column.getTableName())
                .map(SqlCleanser::escape)
                .orElseThrow(IllegalArgumentException::new);

        String columnName = ofNullable(column.getColumnName())
                .map(SqlCleanser::escape)
                .orElseThrow(IllegalArgumentException::new);

        String closingParenthesisString = this.closingParenthesis.stream().map(Enum::toString).collect(Collectors.joining());

        if (schema == null || schema.equals("null")) {
            return String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.getFilter(),
                    closingParenthesisString);
        } else {
            return String.format(" %s %s%s%s%s.%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, schema, endingDelimiter,
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.getFilter(),
                    closingParenthesisString);
        }
    }

    /**
     * Walks the criteria tree and returns a SQL string representation of this criterion and all it's childCriteria.  Use
     * this method when you want this criterion's SQL string representation AND it's childCriteria's SQL string
     * representations.
     *
     * @param beginningDelimiter The beginning delimiter based on the SQL dialect.
     * @param endingDelimiter The ending delimiter based on the SQL dialect.
     * @param criteriaSqlStringHolder An object that encapsulates the list of crtierion SQL string representations that
     *                                acts as a holder/container for the criterion and it's children as it's passed through
     *                                the tree.
     * @return The SQL string representation of the criteria tree.
     */
    public void toSqlDeep(char beginningDelimiter, char endingDelimiter, CriteriaSqlStringHolder criteriaSqlStringHolder) throws IllegalArgumentException {
        // If this criterion is a root criterion (meaning, it has no parent), then reset the criteriaSqlStringHolder.
        if (this.isRoot()) {
            this.openingParenthesis = Parenthesis.Empty;
            criteriaSqlStringHolder.resetNumOfOpeningAndClosingParenthesis();
        }

        // If the criterion is somewhere in the middle of the tree - it is not a root criterion, but it is a parent of child
        // criteria.
        if (! this.isRoot() && this.isParent()) {
            this.openingParenthesis = Parenthesis.FrontParenthesis;
        }

        // If criterion is the last child in this branch of the tree, add the necessary number of closing parenthesis.
        if (this.isLastChildInBranch()) {
            int numClosingParenthesisToAdd = criteriaSqlStringHolder.getDiffOfOpeningAndClosingParenthesis();

            for (int i=0; i<numClosingParenthesisToAdd; i++) {
                this.closingParenthesis.add(Parenthesis.EndParenthesis);
            }
        }

        // Get this criterion's SQL string representation.
        criteriaSqlStringHolder.addSqlString(this, beginningDelimiter, endingDelimiter);

        // Call this method for each of this criterion's childCriteria (if it has any).
        for (Criterion childCriterion : this.childCriteria) {
            childCriterion.toSqlDeep(beginningDelimiter, endingDelimiter, criteriaSqlStringHolder);
        }
    }

    public boolean isValid() {
        // Column and operator must always be non-null.
        if (column == null || operator == null) {
            return false;
        }

        // If operator is not `isNotNull` and `isNull` and filter is null or an empty string, then criterion is not valid.
        // In other words, the criterion has an operator that expects a non-null or non-empty filter, but the filter is
        // null or an empty string.
        if ((operator != Operator.isNotNull && operator != Operator.isNull) && (filter == null || filter.equals(""))) {
            return false;
        }

        return sqlIsClean(this);
    }

    public boolean hasSearchOperator() {
        return this.operator != null && (this.operator.equals(Operator.like) || this.operator.equals(Operator.notLike));
    }

}
