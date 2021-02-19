package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.config.Qb4jConfig;
import net.querybuilder4j.dao.query_template.InMemoryQueryTemplateDaoImpl;
import net.querybuilder4j.dao.query_template.QueryTemplateDao;
import net.querybuilder4j.dao.query_template.QueryTemplateRepositoryType;
import net.querybuilder4j.dao.query_template.SqlDatabaseQueryTemplateDaoImpl;
import net.querybuilder4j.exceptions.QueryTemplateRepositoryTypeNotRecognizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryTemplateDaoFactory extends AbstractFactoryBean<QueryTemplateDao> {

    private final Qb4jConfig qb4jConfig;

    @Autowired
    public QueryTemplateDaoFactory(Qb4jConfig qb4jConfig) {
        this.qb4jConfig = qb4jConfig;
        this.setSingleton(true);
    }


    @Override
    public Class<?> getObjectType() {
        return QueryTemplateDao.class;
    }

    @Override
    protected QueryTemplateDao createInstance() throws Exception {
        QueryTemplateRepositoryType repositoryType = this.qb4jConfig.getQueryTemplateDataSource().getRepositoryType();

        if (repositoryType.equals(QueryTemplateRepositoryType.IN_MEMORY)) {
            return new InMemoryQueryTemplateDaoImpl();
        }
        else if (repositoryType.equals(QueryTemplateRepositoryType.SQL_DATABASE)) {
            return new SqlDatabaseQueryTemplateDaoImpl(this.qb4jConfig);
        }
        else {
            throw new QueryTemplateRepositoryTypeNotRecognizedException(repositoryType.toString());
        }
    }
}
