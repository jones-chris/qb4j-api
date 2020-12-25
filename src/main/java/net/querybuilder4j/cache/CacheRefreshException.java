package net.querybuilder4j.cache;

/**
 * A class representing an exception relating to the refreshing of the database metadata cache.
 */
public class CacheRefreshException extends RuntimeException {

    public CacheRefreshException(String message) {
        super(message);
    }

    public CacheRefreshException(Throwable e) {
        super(e);
    }

}
