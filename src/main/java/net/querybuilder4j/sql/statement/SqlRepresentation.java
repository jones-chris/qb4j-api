package net.querybuilder4j.sql.statement;

public interface SqlRepresentation {

    String toSql(char beginningDelimiter, char endingDelimiter);

}
