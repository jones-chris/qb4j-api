package net.querybuilder4j.sql.statement.criterion;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.querybuilder4j.sql.builder.SqlValidator;
import net.querybuilder4j.sql.statement.SqlRepresentation;
import net.querybuilder4j.sql.statement.column.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static net.querybuilder4j.sql.statement.criterion.Operator.*;

@ToString
@EqualsAndHashCode
public class Criterion implements SqlRepresentation {

    private int id;

    private Criterion parentCriterion;

    private Parenthesis openingParenthesis = Parenthesis.Empty;

    private Conjunction conjunction;

    private Column column;

    private Operator operator;

    private Filter filter;

    private List<Parenthesis> closingParenthesis = new ArrayList<>();

    private List<Criterion> childCriteria = new ArrayList<>();

    public Criterion(int id, Criterion parentCriterion, Conjunction conjunction, Column column, Operator operator, Filter filter,
                     List<Criterion> childCriteria) {
        this.id = id;
        this.parentCriterion = parentCriterion;
        this.conjunction = conjunction;
        this.column = column;
        this.operator = operator;

        this.filter = Objects.requireNonNullElseGet(filter, Filter::new);

        if (childCriteria != null) {
            this.childCriteria = childCriteria;
        }
    }

    public int getId() {
        return id;
    }

    public Criterion getParentCriterion() {
        return parentCriterion;
    }

    public Parenthesis getOpeningParenthesis() {
        return ofNullable(this.openingParenthesis).orElse(Parenthesis.Empty);
    }

    public void setOpeningParenthesis(Parenthesis parenthesis) {
        this.openingParenthesis = parenthesis;
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

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(Filter filter) {
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

    public boolean isParent() {
        return this.childCriteria != null && ! this.childCriteria.isEmpty();
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
     * @param beginningDelimiter The beginning delimiter for the SQL dialect.
     * @param endingDelimiter The ending delimiter for the SQL dialect.
     * @return {@link String}
     */
    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        String schema = ofNullable(column.getSchemaName())
                .map(SqlValidator::escape)
                .orElse(null);

        String table = ofNullable(column.getTableName())
                .map(SqlValidator::escape)
                .orElseThrow(IllegalArgumentException::new);

        String columnName = ofNullable(column.getColumnName())
                .map(SqlValidator::escape)
                .orElseThrow(IllegalArgumentException::new);

        String closingParenthesisString = this.closingParenthesis.stream().map(Enum::toString).collect(Collectors.joining());

        if (schema == null || schema.equals("null")) {
            return String.format(" %s %s%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.filter.toSql(this.operator),
                    closingParenthesisString);
        } else {
            return String.format(" %s %s%s%s%s.%s%s%s.%s%s%s %s %s%s ",
                    this.getConjunction(),
                    this.getOpeningParenthesis(),
                    beginningDelimiter, schema, endingDelimiter,
                    beginningDelimiter, table, endingDelimiter,
                    beginningDelimiter, columnName, endingDelimiter,
                    this.getOperator(),
                    this.filter.toSql(this.operator),
                    closingParenthesisString);
        }
    }

    public boolean hasSearchOperator() {
        return this.operator != null && (this.operator.equals(like) || this.operator.equals(notLike));
    }

    public boolean hasMultipleValuesOperator() {
        return this.operator != null && (this.operator.equals(in) || this.operator.equals(notIn));
    }

}
