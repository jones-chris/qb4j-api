package net.querybuilder4j.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * A conditional bean class that will be instantiated if the `env` property in the application.properties file is set to
 * a value of `local`.  The difference between this class and the `NonLocalDataConfig` class is that this class will
 * read the contents of the `qb4j.yml` file whereas the `LocalDataConfig` class will read the contents of the `qb4jConfig`
 * system property.
 */
@Configuration
@ConditionalOnProperty(
        prefix = "qb4j",
        name = "env",
        havingValue = "nonLocal"
)
public class NonLocalDataConfig {

    final Logger LOG  = LoggerFactory.getLogger(NonLocalDataConfig.class);

//    @ConditionalOnProperty(
//            prefix = "qb4j",
//            name = "env",
//            havingValue = "local"
//    )
//    @Bean(name = "qb4jConfigLocal")
//    public Qb4jConfig getTargetDatabasesLocal() throws IOException {
//        InputStream qb4jConfig = this.getClass()
//                .getClassLoader()
//                .getResourceAsStream("qb4j.yml");
//
//        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//        JsonNode node = mapper.readTree(qb4jConfig);
//
//        LOG.info("Here is the qb4jConfig: \n {}", node.toPrettyString());
//
//        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
//    }

    @Bean
    public Qb4jConfig getTargetDatabases() throws IOException {
        String qb4jConfig = System.getProperty("qb4jConfig");
        LOG.info("Here is the qb4jConfig: \n {}", qb4jConfig);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qb4jConfig);
        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
    }

}
