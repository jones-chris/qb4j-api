package net.querybuilder4j.config;

import net.querybuilder4j.constants.DatabaseType;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class Qb4jConfig {

    private List<TargetDataSource> targetDataSources;
    private QueryTemplateDataSource queryTemplateDataSource;

    public Qb4jConfig() {}

    public Qb4jConfig(List<TargetDataSource> targetDataSources,
                      QueryTemplateDataSource  queryTemplateDataSource) {
        this.targetDataSources = targetDataSources;
        this.queryTemplateDataSource = queryTemplateDataSource;
    }

    public List<TargetDataSource> getTargetDataSources() {
        return targetDataSources;
    }

    public void setTargetDataSources(List<TargetDataSource> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    public QueryTemplateDataSource getQueryTemplateDataSource() {
        return queryTemplateDataSource;
    }

    public void setQueryTemplateDataSource(QueryTemplateDataSource queryTemplateDataSource) {
        this.queryTemplateDataSource = queryTemplateDataSource;
    }

    public List<DataSource> getTargetDataSourcesAsDataSource() {
        return this.targetDataSources.stream().map(TargetDataSource::getDataSource).collect(Collectors.toList());
    }

    public TargetDataSource getTargetDataSource(String name) {
        return this.targetDataSources.stream()
                .filter(source -> source.getName().equals(name))
                .findFirst()
                .get();
    }

    public DataSource getTargetDataSourceAsDataSource(String targetDatabaseName) {
        Optional<TargetDataSource> matchingTargetDataSources = targetDataSources.stream()
                .filter(source -> source.getName().equals(targetDatabaseName))
                .findFirst();

        if (matchingTargetDataSources.isPresent()) {
            TargetDataSource targetDataSource = matchingTargetDataSources.get();
            return targetDataSource.getDataSource();
        } else {
            throw new IllegalArgumentException(String.format("Cannot find target data source with name, %s", targetDatabaseName));
        }
    }

    public static class TargetDataSource {

        private String name;
        private String url;
        private String driverClassName;
        private DatabaseType databaseType;
        private String username;
        private String password;
        private ExcludeObjects excludeObjects;
        private DataSource dataSource;

        public TargetDataSource() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public DatabaseType getDatabaseType() {
            return databaseType;
        }

        public void setDatabaseType(DatabaseType databaseType) {
            this.databaseType = databaseType;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public ExcludeObjects getExcludeObjects() {
            return excludeObjects;
        }

        public void setExcludeObjects(ExcludeObjects excludeObjects) {
            this.excludeObjects = excludeObjects;
        }

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                BasicDataSource ds = new BasicDataSource();
                ds.setDriverClassName(driverClassName);
                ds.setUrl(url);
                ds.setUsername(username);
                ds.setPassword(password);

                this.dataSource = ds;
            }
            return this.dataSource;
        }

        public Properties getProperties() {
            Properties properties = new Properties();
            properties.setProperty("driver-class-name", driverClassName);
            properties.setProperty("url", url);
            if (username != null) {
                properties.setProperty("username", username);
            }
            if (password != null) {
                properties.setProperty("password", password);
            }
            properties.setProperty("databaseType", databaseType.toString());

            return properties;
        }

        public static class ExcludeObjects {

            /**
             * A list of the names of the schemas to exclude (case-sensitive).
             */
            private List<String> schemas = new ArrayList<>();

            /**
             * A list of the fully qualified names of the tables to exclude (case-sensitive).
             */
            private List<String> tables = new ArrayList<>();

            /**
             * A list of the fully qualified names of the columns to exclude (case-sensitive).
             */
            private List<String> columns = new ArrayList<>();

            public List<String> getSchemas() {
                return schemas;
            }

            public void setSchemas(List<String> schemas) {
                this.schemas = schemas;
            }

            public List<String> getTables() {
                return tables;
            }

            public void setTables(List<String> tables) {
                this.tables = tables;
            }

            public List<String> getColumns() {
                return columns;
            }

            public void setColumns(List<String> columns) {
                this.columns = columns;
            }
        }
    }

    public static class QueryTemplateDataSource {
        private String url;
        private String driverClassName;
        private String username;
        private String password;
        private DataSource dataSource;

        public QueryTemplateDataSource() {}

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                BasicDataSource ds = new BasicDataSource();
                ds.setDriverClassName(driverClassName);
                ds.setUrl(url);
                ds.setUsername(username);
                ds.setPassword(password);

                this.dataSource = ds;
            }
            return this.dataSource;
        }
    }

}
