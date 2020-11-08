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

@Configuration
@ConditionalOnProperty(
        prefix = "qb4j",
        name = "env",
        havingValue = "local"
)
public class LocalDataConfig {

    final Logger LOG  = LoggerFactory.getLogger(LocalDataConfig.class);

    @Bean
    public Qb4jConfig getTargetDatabases() throws IOException {
        InputStream qb4jConfig = this.getClass()
                .getClassLoader()
                .getResourceAsStream("qb4j.yml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qb4jConfig);

        LOG.info("Here is the qb4jConfig: \n {}", node.toPrettyString());

        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
    }

}
