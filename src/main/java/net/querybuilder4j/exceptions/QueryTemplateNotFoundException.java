package net.querybuilder4j.exceptions;

public class QueryTemplateNotFoundException extends RuntimeException {

    public QueryTemplateNotFoundException(String queryTemplateName) {
        super("Did not find query template with name, " + queryTemplateName);
    }

    public QueryTemplateNotFoundException(String queryTemplateName, int version) {
        super("Did not find query template with name, " + queryTemplateName + ", and version, " + version);
    }

}
