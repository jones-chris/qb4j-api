package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.exceptions.QueryTemplateNotFoundException;
import net.querybuilder4j.sql.statement.SelectStatement;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryQueryTemplateDaoImpl implements QueryTemplateDao {

    private final Set<SelectStatement> queryTemplates = new HashSet<>();

    @Override
    public SelectStatement findByName(String name, int version) {
        return this.queryTemplates.stream()
                .filter(template -> template.getMetadata().getName().equals(name) && template.getMetadata().getVersion() == version)
                .findFirst()
                .orElseThrow(() -> new QueryTemplateNotFoundException(name));
    }

//    @Override
//    public Map<String, SelectStatement> findByNames(List<String> names) {
//        Map<String, SelectStatement> templates = new HashMap<>();
//
//        // todo:  Research if there is a more efficient way to do this where a new stream is not being created for each
//        // todo:  ...iteration of the forEach loop.
//        names.forEach(name -> {
//            this.queryTemplates.stream()
//                    .filter(template -> template.getMetadata().getName().equals(name))
//                    .findFirst()
//                    .ifPresentOrElse(
//                            (template) -> templates.put(name, template),
//                            () -> {
//                                throw new QueryTemplateNotFoundException(name);
//                            }
//                    );
//        });
//
//        return templates;
//    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        return this.queryTemplates.add(selectStatement);
    }

    @Override
    public List<String> listNames() {
        return this.queryTemplates.stream()
                .map(template -> template.getMetadata().getName())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Integer> getNewestVersion(String name) {
        return this.queryTemplates.stream()
                .filter(template -> template.getMetadata().getName().equals(name)) // Get all query templates that match the name.
                .sorted(Comparator.comparingInt(template -> ((SelectStatement) template).getMetadata().getVersion()).reversed()) // Sort in descending order (highest version to lowest version).
                .map(template -> template.getMetadata().getVersion()) // Get a stream of the query templates' versions.
                .findFirst(); // Get the first version (the highest version after sorting).
    }

    @Override
    public List<Integer> getVersions(String name) {
        return this.queryTemplates.stream()
                .filter(template -> template.getMetadata().getName().equals(name))
                .sorted(Comparator.comparingInt(template -> template.getMetadata().getVersion()))
                .map(template -> template.getMetadata().getVersion())
                .collect(Collectors.toList());
    }

    @Override
    public SelectStatement.Metadata getMetadata(String name, int version) {
        return this.queryTemplates.stream()
                .filter(template -> template.getMetadata().getName().equals(name) && template.getMetadata().getVersion() == version)
                .findFirst()
                .map(SelectStatement::getMetadata)
                .orElseThrow(() -> new QueryTemplateNotFoundException(name, version));
    }

}
