package net.querybuilder4j.config;

import net.querybuilder4j.cache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseMetadataCacheFactory extends AbstractFactoryBean<DatabaseMetadataCache> {

    private final Qb4jConfig qb4jConfig;

    @Autowired
    public DatabaseMetadataCacheFactory(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
        this.setSingleton(true);  // Always create singletons regardless of the DatabaseMetadataCache implementation.
    }

    @Override
    public Class<?> getObjectType() {
        return DatabaseMetadataCache.class;
    }

    @Override
    protected DatabaseMetadataCache createInstance() {
        CacheType cacheType = this.qb4jConfig.getDatabaseMetadataCacheSource().getCacheType();

        if (cacheType.equals(CacheType.IN_MEMORY)) {
            return new InMemoryDatabaseMetadataCacheImpl(this.qb4jConfig);
        } else if (cacheType.equals(CacheType.REDIS)) {
            return new RedisDatabaseMetadataCacheImpl(this.qb4jConfig);
        } else {
            throw new CacheTypeNotRecognizedException(cacheType + " is not a recognized database metadata cache type");
        }
    }
}
