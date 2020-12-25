package net.querybuilder4j.cache;

/**
 * A class that should be raised if the {@link CacheType} is not recognized in the
 * @{@link net.querybuilder4j.config.DatabaseMetadataCacheFactory} class.
 */
public class CacheTypeNotRecognizedException extends RuntimeException {

    public CacheTypeNotRecognizedException(String message) {
        super(message);
    }

}
