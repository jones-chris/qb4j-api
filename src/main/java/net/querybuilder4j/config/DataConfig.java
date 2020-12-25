package net.querybuilder4j.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * The class that will instantiate a {@link Qb4jConfig}.
 */
@Configuration
public class DataConfig {

    final Logger LOG  = LoggerFactory.getLogger(DataConfig.class);

    @Bean
    public Qb4jConfig getTargetDatabases() throws IOException {
        String qb4jConfig = System.getProperty("qb4jConfig");
        LOG.info("Here is the qb4jConfig: \n {}", qb4jConfig);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qb4jConfig);
        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
    }

}
