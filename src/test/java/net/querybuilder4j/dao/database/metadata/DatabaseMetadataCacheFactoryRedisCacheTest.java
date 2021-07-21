package net.querybuilder4j.dao.database.metadata;

import net.querybuilder4j.config.QbConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(DatabaseMetadataCacheFactoryRedisCacheTest.DataConfig.class)
@ContextConfiguration(classes = {
        DatabaseMetadataCacheFactory.class,
        DatabaseMetadataCrawlerDao.class
})
public class DatabaseMetadataCacheFactoryRedisCacheTest {

    @MockBean
    private DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao;

    @Autowired
    private DatabaseMetadataCacheFactory databaseMetadataCacheFactory;

    @TestConfiguration
    public static class DataConfig {

        @Bean
        public QbConfig buildQbConfig() {
            QbConfig.DatabaseMetadataCacheSource databaseMetadataCacheSource = new QbConfig.DatabaseMetadataCacheSource();
            databaseMetadataCacheSource.setCacheType(CacheType.REDIS);

            QbConfig qbConfig = new QbConfig();
            qbConfig.setDatabaseMetadataCacheSource(databaseMetadataCacheSource);

            return qbConfig;
        }

    }

    @Test
    public void createInstance_redisCacheTypeInstantiatesAnRedisDatabaseMetadataCacheDaoImpl() {
        DatabaseMetadataCacheDao databaseMetadataCacheDao = this.databaseMetadataCacheFactory.createInstance();

        assertTrue(databaseMetadataCacheDao instanceof RedisDatabaseMetadataCacheDaoImpl);
    }

}
