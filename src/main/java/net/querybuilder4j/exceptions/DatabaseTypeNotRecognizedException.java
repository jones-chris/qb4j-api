package net.querybuilder4j.exceptions;

import net.querybuilder4j.constants.DatabaseType;

public class DatabaseTypeNotRecognizedException extends RuntimeException {

    public DatabaseTypeNotRecognizedException(DatabaseType databaseType) {
        super("Database type, " + databaseType + ", not recognized");
    }

}
