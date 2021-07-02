package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.exceptions.QueryTemplateRepositoryTypeNotRecognizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryTemplateDaoFactory extends AbstractFactoryBean<QueryTemplateDao> {

    private final QbConfig qbConfig;

    @Autowired
    public QueryTemplateDaoFactory(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
        this.setSingleton(true);
    }


    @Override
    public Class<?> getObjectType() {
        return QueryTemplateDao.class;
    }

    @Override
    protected QueryTemplateDao createInstance() throws Exception {
        QueryTemplateRepositoryType repositoryType = this.qbConfig.getQueryTemplateDataSource().getRepositoryType();

        if (repositoryType.equals(QueryTemplateRepositoryType.IN_MEMORY)) {
            return new InMemoryQueryTemplateDaoImpl();
        }
        else if (repositoryType.equals(QueryTemplateRepositoryType.SQL_DATABASE)) {
            return new SqlDatabaseQueryTemplateDaoImpl(this.qbConfig);
        }
        else {
            throw new QueryTemplateRepositoryTypeNotRecognizedException(repositoryType.toString());
        }
    }
}
