package net.querybuilder4j.sql.statement.criterion;

import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCache;
import net.querybuilder4j.sql.statement.validator.DatabaseMetadataCacheValidator;

import java.util.*;
import java.util.stream.Collectors;

import static net.querybuilder4j.sql.builder.SqlCleanser.escape;

public class CriteriaTreeFlattener {

    /**
     * The original, un-flattened criteria.
     */
    private List<Criterion> unflattenedCriteria = new ArrayList<>();

    /**
     * The flattened criteria that are written here as the tree is walked.
     * The key is just an index representing each root's tree.  The value is the flattened tree for that root criterion.
     */
    private Map<Integer, List<Criterion>> flattenedCriteria = new HashMap<>();

    /**
     * The count of the opening parentheses in the current tree.
     */
    private int numOfOpeningParenthesisInBranch = 0;

    /**
     * The count of the closing parentheses in the current tree.
     */
    private int numOfClosingParenthesisInBranch = 0;

    /**
     * The cache of the target data source(s) and query template data source, which is built from the Qb4jConfig.json file.
     */
    protected DatabaseMetadataCache databaseMetadataCache;

    /**
     * The class responsible for validating the various fields in the `selectStatement`.
     */
    protected DatabaseMetadataCacheValidator databaseMetadataCacheValidator;

    public CriteriaTreeFlattener(List<Criterion> criteria,
                                 DatabaseMetadataCache databaseMetadataCache,
                                 DatabaseMetadataCacheValidator databaseMetadataCacheValidator) {
        this.unflattenedCriteria = criteria;
        this.databaseMetadataCache = databaseMetadataCache;
        this.databaseMetadataCacheValidator = databaseMetadataCacheValidator;
        this.flattenedCriteria = flattenCriteria(criteria, new HashMap<>());
        this.addParenthesis();
        this.quoteCriteriaFilterItems();
    }

    /**
     * Get the entire flattened criteria tree as a SQL string.
     *
     * @param beginningDelimiter The beginning delimiter for the SQL dialect.
     * @param endingDelimiter The ending delimiter for the SQL dialect.
     * @return A SQL string representing the entire flattened criteria tree.
     */
    public String getSqlStringRepresentation(char beginningDelimiter, char endingDelimiter) {
        return String.join(" ", this.getCriterionSqlStrings(beginningDelimiter, endingDelimiter));
    }

    public List<String> getCriterionSqlStrings(char beginningDelimiter, char endingDelimiter) {
        return this.flattenedCriteria.entrySet().stream()
                .flatMap(key -> key.getValue().stream().map(criterion -> {
                    return criterion.toSql(beginningDelimiter, endingDelimiter);
                }))
                .collect(Collectors.toList());
    }

    /**
     * Walks the criteria tree and returns a SQL string representation of this criterion and all it's childCriteria.  Use
     * this method when you want this criterion's SQL string representation AND it's childCriteria's SQL string
     * representations.
     */
    private void addParenthesis() {
        this.flattenedCriteria.forEach((rootIndex, criteria) -> {
            this.resetNumOfOpeningAndClosingParenthesis();

            criteria.forEach(criterion -> {
                // If the criterion is first root, then set conjunction to empty.
                if (rootIndex == 0 && criterion.isRoot()) {
                    criterion.setConjunction(Conjunction.Empty);
                }

                // If the criterion is a parent, then add an opening/front parenthesis.
                if (criterion.isParent()) {
                    this.addOpeningParenthesis(criterion);
                }

                // If criterion is the last child in this branch of the tree, add the necessary number of closing parenthesis.
                if (this.isEndOfBranch(rootIndex, criterion)) {
                    this.addClosingParenthesis(criterion);
                }

                // If the criterion is the last child in the tree (the last criterion in the root's list of criteria, then
                // add the necessary number of closing parenthesis so that we have the same number of opening and closing
                // parenthesis.
                if (this.isEndOfTree(rootIndex, criterion)) {
                    int numClosingParenthesisToAdd = this.getParenthesisCountDifference();

                    for (int i=0; i<numClosingParenthesisToAdd; i++) {
                        this.addClosingParenthesis(criterion);
                    }
                }
            });

            // todo:  This final check may not be needed anymore?
            // Determine if any remaining closing parenthesis are needed at end of criteria.  This only applies to criteria
            // lists that end on a child criteria.
            int parenthesisCountDifference = this.getParenthesisCountDifference();
            if (parenthesisCountDifference != 0) {
                Criterion lastCriterion = this.flattenedCriteria.get(rootIndex).get(flattenedCriteria.size() - 1);
                for (int i=0; i<parenthesisCountDifference; i++) {
                    lastCriterion.getClosingParenthesis().add(Parenthesis.EndParenthesis);
                }
            }
        });
    }

    /**
     * A convenience method for setting the opening parenthesis of a criterion to be a front/opening parenthesis.  This
     * method increments the `numOfOpeningParenthesisInBranch` field by 1.  Use this method instead of incrementing the
     * field, so that the actual front/opening parenthesis and the count in the `numOfOpeningParenthesisInBranch` do not
     * become disjointed.
     *
     * @param criterion The criterion to add the front/opening parenthesis to.
     */
    private void addOpeningParenthesis(Criterion criterion) {
        criterion.setOpeningParenthesis(Parenthesis.FrontParenthesis);
        this.numOfOpeningParenthesisInBranch++;
    }

    /**
     * A convenience method for adding a closing/ending parenthesis to a criterion.  This method increments the
     * `numOfClosingParenthesisInBranch` field by 1.  Use this method instead of incrementing the field, so that the
     * actual closing/ending parenthesis and the count in the `numOfClosingParenthesisInBranch` do not become disjointed.
     *
     * @param criterion The criterion to add the front/opening parenthesis to.
     */
    private void addClosingParenthesis(Criterion criterion) {
        criterion.getClosingParenthesis().add(Parenthesis.EndParenthesis);
        this.numOfClosingParenthesisInBranch++;
    }

    /**
     * Gets the difference between the count of the opening and closing parentheses.
     *
     * @return The difference.
     */
    private int getParenthesisCountDifference() {
        return this.numOfOpeningParenthesisInBranch - this.numOfClosingParenthesisInBranch;
    }

    /**
     * Resets the count of the opening and closing parentheses.
     */
    private void resetNumOfOpeningAndClosingParenthesis() {
        this.numOfOpeningParenthesisInBranch = 0;
        this.numOfClosingParenthesisInBranch = 0;
    }

    /**
     * Walks the criteria tree recursively in a depth-first fashion and returns a flattened list of the criteria tree.
     *
     * @param criteria The criteria tree to walk.
     */
    public static Map<Integer, List<Criterion>> flattenCriteria(List<Criterion> criteria, Map<Integer, List<Criterion>> flattenedCriteriaHolder) {
        for (Criterion criterion : criteria) {
            // If the criterion is a root, then create a new entry pair in the `flattenedCriteria` map.
            if (criterion.getParentCriterion() == null) {
                flattenedCriteriaHolder.put(
                        flattenedCriteriaHolder.size(),
                        new ArrayList<>(Collections.singletonList(criterion)) // Has to be wrapped so that the list is mutable.
                );
            } else {
                // If the criterion is a child, then add it to the current entry pair value because it must be under the
                // same root criterion.
                flattenedCriteriaHolder.get(
                        flattenedCriteriaHolder.size() - 1 // Always assume we're on the latest root criterion while walking the tree.
                ).add(criterion);
            }

            // Continue to walk the tree's depth.
            flattenCriteria(criterion.getChildCriteria(), flattenedCriteriaHolder);
        }

        return flattenedCriteriaHolder;
    }

    /**
     * Checks whether the criterion is the last criterion in the current branch (not the whole root tree).
     *
     * @param rootIndex The root index, which will be used to retrieve the root criteria from the `flattenedCriteria` field
     *                  of this class.
     * @param criterion The criterion to check whether it's the end of a branch.
     * @return Whether the criterion is the end of a branch in the given root/tree.
     */
    private boolean isEndOfBranch(int rootIndex, Criterion criterion) {
        // In order to be the end of the branch, the criterion must not be a parent (ie:  it must not have any children).
        if (! criterion.isParent()) {
            int criterionIndex = this.flattenedCriteria.get(rootIndex).indexOf(criterion);
            int lastIndexOfParentCriterion = this.getLastIndexOfCriteriaAsParent(rootIndex, criterion.getParentCriterion());
            return criterionIndex == lastIndexOfParentCriterion;
        }

        return false;
    }

    /**
     * Find the index of the last criterion that has this parentCriterion.
     *
     * @param rootIndex The root index, which will be used to retrieve the root criteria from the `flattenedCriteria` field
     *                   of this class.
     * @param parentCriterion The parent criterion to find the index of where it is last referenced as a parent.
     * @return The index in the root/tree representing where the parent criterion is last referenced as a parent.
     */
    private int getLastIndexOfCriteriaAsParent(int rootIndex, Criterion parentCriterion) {
        if (parentCriterion == null) {
            return -1;
        }

        for (int i=this.flattenedCriteria.get(rootIndex).size() - 1; i>=0; i--) {
            if (this.flattenedCriteria.get(rootIndex).get(i).getParentCriterion() == null) {
                continue;
            }
            if (this.flattenedCriteria.get(rootIndex).get(i).getParentCriterion().equals(parentCriterion)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks whether the criterion is the last criterion in the flattened criteria tree for the root index.
     *
     * @param rootIndex The root index, which will be used to retrieve the root criteria from the `flattenedCriteria` field
     *                  of this class.
     * @param criterion The criterion to check whether it's the end of a branch.
     * @return Whether the criterion is the end of the given root/tree.
     */
    private boolean isEndOfTree(int rootIndex, Criterion criterion) {
        List<Criterion> criteria = this.flattenedCriteria.get(rootIndex);
        return criteria.get(criteria.size() - 1).equals(criterion);
    }

    /**
     * Wrap each column's filter items (after splitting on ",") in quotes based on the column's data type.
     */
    private void quoteCriteriaFilterItems() {
        for (Map.Entry<Integer, List<Criterion>> entry : this.flattenedCriteria.entrySet()) {
            for (Criterion criterion : entry.getValue()) {
                String[] filterItems = criterion.getFilter().getValues().toArray(String[]::new);
                String[] newFilterItems = filterItems.clone();
                for (int i=0; i<filterItems.length; i++) {
                    String filterItem = filterItems[i];

                    // If the filter item is a sub query, then continue, because this method was already run when building
                    // the sub query (so it does not need escaping) and it does not need to be quoted.
                    if (this.isSubQuery(filterItem)) {
                        continue;
                    }

                    filterItem = escape(filterItem);

                    // If the criterion's operator is a "search" operator (LIKE or NOT LIKE), then wrap the filter in single
                    // quotes so that the database will treat it as a string search regardless of whether the column type
                    // should be wrapped or not.
                    if (criterion.hasSearchOperator()) {
                        filterItem = String.format("'%s'", filterItem);
                    } else {
                        // If the criterion's filter does not contain a search character, then proceed as normal but getting
                        // the column's data type from the cache, because we don't trust the column's data type that the client
                        // sent.
                        int columnDataType = this.databaseMetadataCache.getColumnDataType(criterion.getColumn());
                        boolean shouldHaveQuotes = this.databaseMetadataCacheValidator.isColumnQuoted(columnDataType);
                        if (shouldHaveQuotes) {
                            filterItem = String.format("'%s'", filterItem);
                        }
                    }

                    newFilterItems[i] = filterItem;
                }

                Filter newFilter = new Filter();
                newFilter.setValues(Arrays.asList(newFilterItems));

                criterion.setFilter(newFilter);
            }
        }
    }

    /**
     * Convenience method check if a filterItem is a sub query SQL string.
     * @param filterItem The {@link Criterion} {@link Filter} value item.
     * @return true if the {@param filterItem} is a subQuery SQL string.  Otherwise, false.
     */
    private boolean isSubQuery(String filterItem) {
        if (filterItem.length() > 0) {
            return filterItem.startsWith("(") && filterItem.contains("SELECT * FROM") && filterItem.endsWith(")");
        }

        return false;
    }

}
