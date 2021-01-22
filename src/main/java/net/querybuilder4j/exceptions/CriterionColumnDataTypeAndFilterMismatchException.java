package net.querybuilder4j.exceptions;

public class CriterionColumnDataTypeAndFilterMismatchException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "A criterion's column's data type is %s, but the filter value, %s is not a %s";

    public CriterionColumnDataTypeAndFilterMismatchException(String jdbcDataType, String filterValue) {
        super(
                String.format(MESSAGE_TEMPLATE, jdbcDataType, filterValue, jdbcDataType)
        );
    }

}
