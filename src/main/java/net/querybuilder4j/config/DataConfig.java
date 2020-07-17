package net.querybuilder4j.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@PropertySource("classpath:application.properties")
public class DataConfig {

    @Value("${environment}")
    private String environment;

    @Bean(name = "qb4jConfig")
    public Qb4jConfig getTargetDatabases() throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/qb4j.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(inputStream);
        return mapper.readValue(node.get(environment).toPrettyString(), Qb4jConfig.class);
    }

}
