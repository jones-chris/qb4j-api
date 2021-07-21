package net.querybuilder4j.config;

import lombok.*;
import net.querybuilder4j.constants.DatabaseType;
import net.querybuilder4j.dao.database.metadata.CacheType;
import net.querybuilder4j.dao.query_template.QueryTemplateRepositoryType;
import net.querybuilder4j.exceptions.QbConfigException;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class QbConfig {

    private List<TargetDataSource> targetDataSources;
    private QueryTemplateDataSource queryTemplateDataSource;
    private DatabaseMetadataCacheSource databaseMetadataCacheSource;

    public QbConfig(
            List<TargetDataSource> targetDataSources,
            QueryTemplateDataSource  queryTemplateDataSource
    ) {
        this.targetDataSources = targetDataSources;
        this.queryTemplateDataSource = queryTemplateDataSource;
    }

    public List<DataSource> getTargetDataSourcesAsDataSource() {
        return this.targetDataSources.stream()
                .map(TargetDataSource::getDataSource)
                .collect(Collectors.toList());
    }

    public TargetDataSource getTargetDataSource(String name) {
        return this.targetDataSources.stream()
                .filter(source -> source.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> {
                    throw new QbConfigException("Could not find a target data source named, " + name + ", to create a DataSource for");
                });
    }

    public DataSource getTargetDataSourceAsDataSource(String targetDatabaseName) {
       return targetDataSources.stream()
               .filter(source -> source.getName().equals(targetDatabaseName))
               .findFirst()
               .map(TargetDataSource::getDataSource)
               .orElseThrow(() -> {
                   throw new IllegalArgumentException(String.format("Cannot find target data source with name, %s", targetDatabaseName));
               });
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class TargetDataSource {

        @Getter
        @Setter
        private String name;

        @Getter
        @Setter
        private String url;

        @Getter
        @Setter
        private DatabaseType databaseType;

        @Getter
        @Setter
        private String username;

        @Getter
        @Setter
        private String password;

        @Getter
        @Setter
        private ExcludeObjects excludeObjects = new ExcludeObjects();

        private DataSource dataSource;

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                BasicDataSource ds = new BasicDataSource();
                ds.setUrl(url);
                ds.setUsername(username);
                ds.setPassword(password);

                // todo:  Add more driver mappings here when more database types are supported.
                if(this.databaseType.equals(DatabaseType.PostgreSQL)) {
                    ds.setDriverClassName("org.postgresql.Driver");
                }
                else if (this.databaseType.equals(DatabaseType.MySql)) {
                    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                }
                else if (this.databaseType.equals(DatabaseType.Sqlite)) {
                    ds.setDriverClassName("org.sqlite.JDBC");
                }

                this.dataSource = ds;
            }
            return this.dataSource;
        }

        @NoArgsConstructor
        @Getter
        @Setter
        @EqualsAndHashCode
        @ToString
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

        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class QueryTemplateDataSource {

        @Getter
        @Setter
        private QueryTemplateRepositoryType repositoryType;

        @Getter
        @Setter
        private String url;

        @Getter
        @Setter
        private DatabaseType databaseType;

        @Getter
        @Setter
        private String username;

        @Getter
        @Setter
        private String password;

        private DataSource dataSource;

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                BasicDataSource ds = new BasicDataSource();
                ds.setUrl(url);
                ds.setUsername(username);
                ds.setPassword(password);

                // todo:  Add more driver mappings here when more database types are supported.
                if(this.databaseType.equals(DatabaseType.PostgreSQL)) {
                    ds.setDriverClassName("org.postgresql.Driver");
                }
                else if (this.databaseType.equals(DatabaseType.MySql)) {
                    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                }
                else if (this.databaseType.equals(DatabaseType.Sqlite)) {
                    ds.setDriverClassName("org.sqlite.JDBC");
                }

                this.dataSource = ds;
            }
            return this.dataSource;
        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    @Setter
    public static class DatabaseMetadataCacheSource {

        private CacheType cacheType;

        private String host;

        private int port;

        private String username;

        private String password;

    }

}
