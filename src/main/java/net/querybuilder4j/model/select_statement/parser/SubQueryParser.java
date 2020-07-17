package net.querybuilder4j.model.select_statement.parser;

import net.querybuilder4j.dao.query_template.QueryTemplateDao;
import net.querybuilder4j.model.select_statement.SelectStatement;
import net.querybuilder4j.sql_builder.SqlBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SubQueryParser {

    /**
     * A Map of the stmt's subqueries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the subquery deserialized into a SelectStatement object.
     */
    protected Map<String, SelectStatement> unbuiltSubQueries = new HashMap<>();

    /**
     * A Map of the stmt's sub queries with the key being the subquery id (subquery0, subquery1, etc) and the value being
     * the SELECT SQL string generated from the SelectStatement object in the subQueries field of this class.
     */
    protected Map<String, String> builtSubQueries = new HashMap<>();

    /**
     * The SelectStatement that encapsulates the data to generate the SELECT SQL string.
     */
    protected SelectStatement stmt;

    /**
     * The DAO that will be used to get query templates.
     */
    private transient QueryTemplateDao queryTemplateDao;

    /**
     * The SqlBuilder object used to build sub queries into SQL string representations.
     */
    private SqlBuilder sqlBuilder;

    public SubQueryParser(SelectStatement stmt, SqlBuilder sqlBuilder) throws Exception {
        this.stmt = stmt;
        this.sqlBuilder = sqlBuilder;

        setSubqueries();

        // First, get all SelectStatements that are listed in subqueries.  Later we will replace the params in each subquery.
        // TODO:  this eager loads the subqueries.  It may be beneficial to consider having a class boolean field for lazy loading.
        if (! this.stmt.getSubQueries().isEmpty()) {
            this.stmt.getSubQueries().forEach((subQueryId, subQueryCall) -> {
                String subQueryName = subQueryCall.substring(0, subQueryCall.indexOf("("));
                SelectStatement queryTemplate = this.queryTemplateDao.findByName(subQueryName);

                if (queryTemplate == null) {
                    throw new RuntimeException(String.format("Could not find subquery named %s in queryTemplateDao", subQueryName));
                } else {
                    this.unbuiltSubQueries.put(subQueryId, queryTemplate);
                }
            });
        }

        buildSubQueries();
    }

    @Autowired
    public void setQueryTemplateDao(QueryTemplateDao queryTemplateDao) {
        this.queryTemplateDao = queryTemplateDao;
    }

    public Map<String, String> getBuiltSubQueries() {
        return builtSubQueries;
    }

    /**
     * Determines if all subqueries are built.
     * @return boolean
     */
    private boolean allSubQueriesAreBuilt() {
        for (String subquery : unbuiltSubQueries.keySet()) {
            if (! builtSubQueries.containsKey(subquery)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if a String is a '$', which is the subquery qb4j expression.  If the String "$" is not at index 0 in the String, then
     * false.  Otherwise, true.
     * @param arg The arg to test whether it is a subquery.
     * @return boolean
     */
    public static boolean argIsSubQuery(String arg) {
        if (arg == null || arg.isEmpty()) {
            return false;
        } else {
            return 0 == arg.toLowerCase().indexOf("$");
        }
    }

    /**
     * Gets all of the subqueries that match the subQueryArgs.  The resulted Map is intended to be used to set a child
     * SelectStatement's subqueries property so that SQL can be generated correctly.
     *
     * @param subQueryArgs An array of subquery args in "param=arg" format.
     * @return Map<String, String>
     */
    private Map<String, String> getRelevantSubQueries(String[] subQueryArgs) {
        Map<String, String> relevantSubQueries = new HashMap<>();
        for (String paramAndArg : subQueryArgs) {
            String arg = paramAndArg.split("=")[1];
            if (argIsSubQuery(arg)) {
                String subQueryCall = this.stmt.getSubQueries().get(arg);
                relevantSubQueries.put(arg, subQueryCall);
            }
        }
        return relevantSubQueries;
    }

    /**
     * Convenience method for retrieving a subQuery SelectStatement by id, calling the SelectStatement setters (if needed),
     * calling toSql() to build the SelectStatement's SQL string, and putting the id and SQL string in the builtSubQueries
     * field.
     *
     * @param subQueryId The idea of the subquery.
     * @param subQueryName The name of the subquery.
     * @param subQueryArgs An array of subquery args in "param=arg" format.
     * @throws Exception If a subquery name cannot be found.
     */
    private void buildSubQuery(String subQueryId, String subQueryName, String[] subQueryArgs) throws Exception {
        SelectStatement stmt = unbuiltSubQueries.get(subQueryId);

        if (stmt == null) {
            String message = String.format("Could not find statement object with name:  %s", subQueryName);
            throw new Exception(message);
        } else {
            if (subQueryArgs.length != 0) {
                stmt.setCriteriaArguments(getSubQueryArgs(subQueryArgs));
                stmt.setSubQueries(getRelevantSubQueries(subQueryArgs));
            }

            String sql = this.sqlBuilder.buildSql();
            builtSubQueries.put(subQueryId, sql);
        }
    }

    /**
     * Returns a Map of a subquery's arguments with the keys being the parameters and the values being the arguments.
     *
     * @param argsArray An array of subquery args in "param=arg" format.
     * @return Map<String, String>
     * @throws Exception If the argsArray parameter is not in "param=arg" format.
     */
    private Map<String, String> getSubQueryArgs(String[] argsArray) throws Exception {
        Map<String, String> args = new HashMap<>();
        for (String paramNameAndArgString : argsArray) {
            if (! paramNameAndArgString.contains("=")) {
                String message = String.format("'%s' is not formatted properly.  It should be 'paramName=argument", paramNameAndArgString);
                throw new Exception(message);
            } else {
                String[] paramAndArgArray = paramNameAndArgString.split("=");
                args.put(paramAndArgArray[0], paramAndArgArray[1]);
            }
        }

        return args;
    }

    /**
     * This method controls building subqueries.
     *
     * The overall flow is that each subquery in this.stmt.subQueries, which contains the
     * raw query name call and arguments, is retrieved using this.queryTemplateDao and deserialized into a SelectStatement,
     * which is added to this.unbuiltSubQueries to await being built.
     *
     * Then, each subquery in this.unbuiltSubQueries is
     * built by calling the toSql() method on each subquery because they are each a SelectStatement object.  When a subquery is
     * built, the resulting SELECT SQL string is added to this.builtSubQueries.
     *
     * Lastly, this.builtSubQueries is referenced by the this.createWhereClause() method to create the WHERE clause of the
     * SELECT SQL string.
     *
     * @throws Exception If the index of "(", ")", or ";" cannot be found.
     */
    protected void buildSubQueries() throws Exception {
        while (! allSubQueriesAreBuilt()) {
            for (Map.Entry<String, String> subQuery : this.stmt.getSubQueries().entrySet()) {
                String subQueryId = subQuery.getKey();
                String subQueryName = subQuery.getValue().substring(0, subQuery.getValue().indexOf("("));
                String[] subQueryArgs = subQuery.getValue().substring(subQuery.getValue().indexOf("(") + 1, subQuery.getValue().indexOf(")")).split(";");

                // If there are no args, then there will be one element in subQueryArgs and it will be an empty string.
                if (subQueryArgs.length == 1 && subQueryArgs[0].equals("")) {
                    subQueryArgs = new String[0];
                }

                if (! builtSubQueries.containsKey(subQueryId)) {
                    buildSubQuery(subQueryId, subQueryName, subQueryArgs);
                }
            }

        }
    }

    /**
     * Automatically sets the subQueries field assuming that the subQuery calls are hand-written into a criterion's filter.
     * If you want to set the subQueries field manually, use the public setSubQueries method.
     */
    //todo:  move to the SubQueryParser class or put in a new class?
    void setSubqueries() throws IllegalArgumentException {
        if (! this.stmt.getCriteria().isEmpty()) {
            this.stmt.getCriteria().forEach((criterion) -> {
                if (SubQueryParser.argIsSubQuery(criterion.getFilter())) {
                    LinkedList<Integer> begSubQueryIndeces = new LinkedList<>();
                    LinkedList<Integer> endSubQueryIndeces = new LinkedList<>();
                    char[] filterChars = criterion.getFilter().toCharArray();

                    for (int i=0; i<filterChars.length; i++) {
                        if (filterChars[i] == '$') {
                            begSubQueryIndeces.add(i);
                        } else if (filterChars[i] == ')') {
                            endSubQueryIndeces.add(i);
                        }
                    }

                    // Check that there are equal number of beginning and ending subQuery indeces - otherwise we have
                    // a malformed subQuery call.
                    if (begSubQueryIndeces.size() == endSubQueryIndeces.size()) {
                        // It's okay to make the while condition based on only one of the LinkedLists because we know at this
                        // point that both LinkedLists are equal sizes.
                        String newFilter = new String(criterion.getFilter());
                        while (begSubQueryIndeces.size() != 0) {
                            String subQueryId = "$" + this.stmt.getSubQueries().size();
                            int begSubQueryIndex = begSubQueryIndeces.removeLast();
                            int endSubQueryIndex = 1000;
                            // Find ending index that is greater than beginning index, but closest to ending index.
                            for (Integer endIndex : endSubQueryIndeces) {
                                if (endIndex > begSubQueryIndex) {
                                    if ((endIndex - begSubQueryIndex) < (endSubQueryIndex - begSubQueryIndex)) {
                                        endSubQueryIndex = endIndex;
                                    }
                                }
                            }
                            endSubQueryIndeces.remove(new Integer(endSubQueryIndex));

                            // Now, get the subQueryCall from filter (which does not change)
                            String subQueryCall = newFilter.substring(begSubQueryIndex + 1, endSubQueryIndex + 1);

                            // Now, look in newFilter (which changes) and replace that subQueryCall with the subQueryId
                            newFilter = newFilter.replace("$" + subQueryCall, subQueryId);

                            // Now, add the subQueryId and subQueryCall to subQueries.
                            this.stmt.getSubQueries().put(subQueryId, subQueryCall);

                            for (int i=0; i<begSubQueryIndeces.size(); i++) {
                                int begElement = begSubQueryIndeces.get(i);
                                if (begElement > begSubQueryIndex) {
                                    int newElement = begElement - (subQueryCall.length());
                                    begSubQueryIndeces.set(i, newElement);
                                }

                                int endElement = endSubQueryIndeces.get(i);
                                if (endElement > endSubQueryIndex) {
                                    int newElement = endElement - (subQueryCall.length()-1);
                                    endSubQueryIndeces.set(i, newElement);
                                }
                            }
                        }
                        criterion.setFilter(newFilter);
                    } else {
                        throw new IllegalArgumentException("SubQuery is malformed");
                    }
                }
            });
        }
    }

}
