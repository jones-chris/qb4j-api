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
import java.io.InputStream;
import java.util.Optional;

/**
 * The class that will instantiate a {@link Qb4jConfig} based on whether the `qb4j.env` property in the application.properties
 * file is set to a value of `local` or `nonLocal`.  If the value is `local`, then this class will read the contents of the `resources/qb4j.yml`
 * file.  If the value is `nonLocal`, then the the class will read the contents of the `qb4jConfig` system property.
 */
@Configuration
public class DataConfig {

    final Logger LOG  = LoggerFactory.getLogger(DataConfig.class);

    @ConditionalOnProperty(
            prefix = "qb4j",
            name = "env",
            havingValue = "local"
    )
    @Bean(name = "qb4jConfigLocal")
    public Qb4jConfig getTargetDatabasesLocal() throws IOException {
        // Try to read the `local-qb4j.yml` file.  If the file cannot be found, then `getResourceAsStream` will return
        // null.
        Optional<InputStream> qb4jConfig = Optional.ofNullable(
                this.getClass()
                .getClassLoader()
                .getResourceAsStream("local-qb4j.yml")
        );

        // If the `local-qb4j.yml` file can be found, then deserialize into a Qb4jConfig object.
        if (qb4jConfig.isPresent()) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            JsonNode node = mapper.readTree(qb4jConfig.get());

            LOG.info("Here is the qb4jConfig: \n {}", node.toPrettyString());

            return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
        }

        // Otherwise, throw an exception.
        throw new Qb4jConfigNotFoundException("local-qb4j.yml could not be found");
    }

    @ConditionalOnProperty(
            prefix = "qb4j",
            name = "env",
            havingValue = "nonLocal"
    )
    @Bean
    public Qb4jConfig getTargetDatabasesNonLocal() throws IOException {
        String qb4jConfig = System.getProperty("qb4jConfig");
        LOG.info("Here is the qb4jConfig: \n {}", qb4jConfig);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qb4jConfig);
        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
    }

}
