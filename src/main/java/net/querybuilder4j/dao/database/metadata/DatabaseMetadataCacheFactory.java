package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.exceptions.CacheTypeNotRecognizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

import static net.querybuilder4j.dao.database.metadata.CacheType.IN_MEMORY;
import static net.querybuilder4j.dao.database.metadata.CacheType.REDIS;

@Configuration
public class DatabaseMetadataCacheFactory extends AbstractFactoryBean<DatabaseMetadataCacheDao> {

    private final QbConfig qbConfig;

    private final DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao = new DatabaseMetadataCrawlerDao();

    @Autowired
    public DatabaseMetadataCacheFactory(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
        this.setSingleton(true);  // Always create singletons regardless of the DatabaseMetadataCacheDao implementation.
    }

    @Override
    public Class<?> getObjectType() {
        return DatabaseMetadataCacheDao.class;
    }

    @Override
    protected DatabaseMetadataCacheDao createInstance() {
        CacheType cacheType = this.qbConfig.getDatabaseMetadataCacheSource().getCacheType();

        if (IN_MEMORY.equals(cacheType)) {
            return new InMemoryDatabaseMetadataCacheDaoImpl(
                    this.qbConfig,
                    this.databaseMetadataCrawlerDao
            );
        } else if (REDIS.equals(cacheType)) {
            return new RedisDatabaseMetadataCacheDaoImpl(
                    this.qbConfig,
                    this.databaseMetadataCrawlerDao
            );
        } else {
            throw new CacheTypeNotRecognizedException(cacheType + " is not a recognized database metadata cache type");
        }
    }
}
