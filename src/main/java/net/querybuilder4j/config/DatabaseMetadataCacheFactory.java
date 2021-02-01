package net.querybuilder4j.config;

import net.querybuilder4j.dao.database.metadata.*;
import net.querybuilder4j.exceptions.CacheTypeNotRecognizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

import static net.querybuilder4j.dao.database.metadata.CacheType.*;

@Configuration
public class DatabaseMetadataCacheFactory extends AbstractFactoryBean<DatabaseMetadataCacheDao> {

    private final Qb4jConfig qb4jConfig;

    @Autowired
    public DatabaseMetadataCacheFactory(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
        this.setSingleton(true);  // Always create singletons regardless of the DatabaseMetadataCacheDao implementation.
    }

    @Override
    public Class<?> getObjectType() {
        return DatabaseMetadataCacheDao.class;
    }

    @Override
    protected DatabaseMetadataCacheDao createInstance() {
        CacheType cacheType = this.qb4jConfig.getDatabaseMetadataCacheSource().getCacheType();

        if (cacheType.equals(IN_MEMORY)) {
            return new InMemoryDatabaseMetadataCacheDaoImpl(this.qb4jConfig);
        } else if (cacheType.equals(REDIS)) {
            return new RedisDatabaseMetadataCacheDaoImpl(this.qb4jConfig);
        } else {
            throw new CacheTypeNotRecognizedException(cacheType + " is not a recognized database metadata cache type");
        }
    }
}
