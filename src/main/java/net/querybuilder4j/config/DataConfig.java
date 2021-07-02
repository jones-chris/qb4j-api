package net.querybuilder4j.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * The class that will instantiate a {@link QbConfig}.
 */
@Configuration
@Slf4j
public class DataConfig {

    @Bean
    public QbConfig getTargetDatabases() throws IOException {
        String qbConfig = System.getProperty("qbConfig");
        log.info("Here is the qbConfig: \n {}", qbConfig);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qbConfig);
        return mapper.readValue(node.toPrettyString(), QbConfig.class);
    }

}
