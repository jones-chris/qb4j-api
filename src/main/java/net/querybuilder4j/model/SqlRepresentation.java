package net.querybuilder4j.model;

public interface SqlRepresentation {

    String toSql(char beginningDelimiter, char endingDelimiter);

}
