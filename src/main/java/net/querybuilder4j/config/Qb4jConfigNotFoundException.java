package net.querybuilder4j.config;

public class Qb4jConfigNotFoundException extends RuntimeException {

    public Qb4jConfigNotFoundException(String message) {
        super(message);
    }

    public Qb4jConfigNotFoundException(Throwable e) {
        super(e);
    }

}
