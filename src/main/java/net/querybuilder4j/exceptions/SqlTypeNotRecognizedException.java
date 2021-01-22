package net.querybuilder4j.exceptions;

public class SqlTypeNotRecognizedException extends RuntimeException {

    public SqlTypeNotRecognizedException(int jdbcType) {
        super(
                String.format("Did not recognize sql type, %s", jdbcType)
        );
    }

}
