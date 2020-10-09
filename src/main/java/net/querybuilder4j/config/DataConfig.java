package net.querybuilder4j.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;

@Configuration
public class DataConfig {

    @Bean(name = "qb4jConfig")
    public Qb4jConfig getTargetDatabases() throws IOException {
        String qb4jConfig = System.getProperty("qb4jConfig");
//        InputStream inputStream = new FileInputStream(new File("/Users/chris.jones/repos/qb4j-api/qb4j.yml"));

        System.out.println("Here is the qb4jConfig:  ");
        System.out.println(qb4jConfig);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readTree(qb4jConfig);
        return mapper.readValue(node.toPrettyString(), Qb4jConfig.class);
    }

}
