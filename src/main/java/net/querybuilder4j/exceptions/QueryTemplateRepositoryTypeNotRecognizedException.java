package net.querybuilder4j.exceptions;

public class QueryTemplateRepositoryTypeNotRecognizedException extends RuntimeException {

    public QueryTemplateRepositoryTypeNotRecognizedException(String repositoryType) {
        super(repositoryType + " is not a recognized QueryTemplateRepositoryType");
    }

}
