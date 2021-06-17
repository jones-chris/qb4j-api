import {store} from "../index";
import {UiMessage} from "../Models/UiMessage";
import {getJdbcSqlType, BIG_INT, BOOLEAN, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT, getCriterionFilterValues} from "../Utils/Utils";
import {flattenCriteria} from "../actions/CriteriaActions";

const NUMERIC_DATA_TYPES = [BIG_INT, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT];

export const assertDatabaseIsSelected = () => {
    if (store.getState().query.selectedDatabase === null) {
        throw Error('Select 1 database');
    }
};

export const assertSubQueriesAreCorrect = () => {
    let subQueries = store.getState().query.subQueries;

    // Each sub query has a name
    subQueries.forEach(subQuery => {
        if (subQuery.subQueryName === null || subQuery.subQueryName === '') {
            throw Error('Each sub query must have a name');
        }

        if (subQuery.subQueryName.includes(' ')) {
            throw Error('Please remove the whitespace from all sub query names');
        }

        // A query template is selected
        if (subQuery.queryTemplateName === null || subQuery.queryTemplateName === '') {
            throw Error(`Please select a query template for sub query, ${subQuery.subQueryName}`);
        }

        // A version is selected
        if (subQuery.version === null || subQuery.version === '') {
            throw Error(`Please select a version for sub query ${subQuery.version}`);
        }

        // Parameters and arguments are correct.
        let parameters = store.getState().query.availableSubQueries[subQuery.queryTemplateName].versions[subQuery.version].metadata.parameters;
        parameters.forEach(parameter => {
            // Check the the right number of arguments exist.
            let args = subQuery.parametersAndArguments[parameter.name];
            if (! parameter.allowsMultipleValues) {
                if (args && args.length > 1) {
                    throw Error(`Parameter, ${parameter.name}, in sub query, ${subQuery.subQueryName}, does not allow multiple arguments`);
                }
            }

            // Check that the data type is correct.
            if (args && args.length > 1) {
                let jdbcDataType = getJdbcSqlType(parameter.column.dataType);
                if (NUMERIC_DATA_TYPES.includes(jdbcDataType)) {
                    args.forEach(arg => {
                        let valueAsNumber = Number(arg);
                        if (isNaN(valueAsNumber)) {
                            throw Error(`In sub query, ${subQuery.subQueryName}, the data type of parameter, ${parameter.name}, is ${jdbcDataType}, 
                                but the argument, ${arg}, is not a(n) ${jdbcDataType}`);
                        }
                    })
                }
            }
        });

        // Each sub query has a unique name.
        let subQueryNamesSorted = subQueries.map(subQuery => subQuery.subQueryName).sort();
        subQueryNamesSorted.forEach((subQueryName, index) => {
            // If this is the last item in the array, continue because adding + 1 to the index in the below 'else if' block will cause an index out of bounds error.
            if (index === subQueryNamesSorted.length - 1) {
                return;
            }
            else if (subQueryName === subQueryNamesSorted[index + 1]) {
                throw Error(`More than one sub query was found with the name, ${subQueryName}.  Each sub query name should be unique.`);
            }
        });
    });
};

export const assertSchemasAreSelected = () => {
    if (store.getState().query.selectedSchemas.length === 0) {
        throw Error('Select 1 or more schema')
    }
};

export const assertTablesAreSelected = () => {
    if (store.getState().query.selectedTables.length === 0) {
        throw Error('Select 1 or more tables');
    }
};

export const assertJoinsExist = () => {
    let queryState = store.getState().query;
    let joinsState = store.getState().joins;
    let numOfTablesAndJoinsDiff = queryState.selectedTables.length - joinsState.joins.length;

    // There should always be 1 more table than joins.
    // At a minimum, there should be 1 less join than tables, because the user must define the join relationship between
    // the tables that have been selected AND could have self-joins in addition.
    if (queryState.selectedTables.length > 1 && (numOfTablesAndJoinsDiff > 1)) {
        throw Error(`You have ${queryState.selectedTables.length} tables selected, but ${joinsState.joins.length} joins.  
        There should be at least ${queryState.selectedTables.length - 1} join(s).`)
    }
};

export const assertColumnsAreSelected = () => {
    if (store.getState().query.selectedColumns.length === 0) {
        throw Error('You must select at least 1 column');
    }
};

export const assertCriteriaOperatorsAreCorrect = () => {
    let criteria = store.getState().query.criteria;
    criteria = flattenCriteria(criteria, []);
    criteria.forEach(criterion => {
        // IN and NOT IN operator check.
        if (criterion.filter.values.length > 1) {
            if (criterion.operator !== 'in' && criterion.operator !== 'notIn') {
                throw Error('A criterion has multiple values, but does not have an IN or NOT IN operator')
            }
        }

        // LIKE or NOT LIKE operator check.
        if (criterion.operator === 'like' || criterion.operator === 'notLike') {

            // The filter should have exactly 1 value when using LIKE or NOT LIKE.
            if (criterion.filter.values.length !== 1) {
                throw Error(`A criterion uses the ${criterion.operator.toUpperCase()} operator, but does not have exactly
                1 filter value`)
            }
        }

        // If the operator is NOT isNull or isNotNull, then filter values should not be empty.
        if (criterion.operator !== 'isNull' && criterion.operator !== 'isNotNull') {
            if (criterion.filter.values.length === 0) {
                throw Error(`A criterion has an empty filter, but has a ${criterion.operator.toUpperCase()} operator`);
            }
        }
    })
};

export const assertCriteriaFiltersAreCorrect = () => {
    let subQueries = store.getState().query.subQueries;
    let availableSubQueries = store.getState().query.availableSubQueries;

    let criteria = store.getState().query.criteria;
    criteria = flattenCriteria(criteria, []);
    criteria.forEach(criterion => {
        // Check that the criterion's filter's values property does not contain an empty string.
        criterion.filter.values.forEach(value => {
            if (value === '') {
                throw Error('The criterion contains an empty/blank string')
            }
        });

        // If data type is not string, then check that the filter values can be converted to int, double, etc.
        let criterionColumnJdbcDataType = getJdbcSqlType(criterion.column.dataType);
        let numericJdbcTypes = [BIG_INT, DECIMAL, DOUBLE, FLOAT, INTEGER, NUMERIC, SMALL_INT, TINY_INT];
        let criterionFilterValuesExcludingParamsAndSubQueries = getCriterionFilterValues(criterion);
        if (numericJdbcTypes.includes(criterionColumnJdbcDataType)) {
            criterionFilterValuesExcludingParamsAndSubQueries.forEach(value => {
                let valueAsNumber = Number(value);
                if (isNaN(valueAsNumber)) {
                    throw Error(`A criterion's column's data type is ${criterionColumnJdbcDataType}, but the filter value, ${value}, is not a(n) ${criterionColumnJdbcDataType}`);
                }
            })
        }

        // Other non-string data type checks.
        if (criterionColumnJdbcDataType === BOOLEAN) {
            criterion.filter.values.forEach(value => {
                let lowerCaseValue = value.toString().toLowerCase();
                if (lowerCaseValue !== 'true' && lowerCaseValue !== 'false') {
                    throw Error(`A criterion's column's data type is ${criterionColumnJdbcDataType}, but the filter value, ${value}, is not a(n) ${criterionColumnJdbcDataType}`);
                }
            })
        }

        // todo:  Add a check for dates and timestamps?

        // Sub query checks.
        criterion.filter.values.forEach(value => {
            if (value.startsWith('$')) {
                let subQueryName = value.substr(1);

                // Check that each sub query can be found.
                let matchingSubQuery = subQueries.find(subQuery => subQuery.subQueryName === subQueryName);
                if (! matchingSubQuery) {
                    throw Error(`The sub query, ${subQueryName}, does not exist`);
                }

                // Check that the sub query only returns one column.
                let availableSubQueryMetadata = store.getState().query.availableSubQueries[matchingSubQuery.queryTemplateName].versions[matchingSubQuery.version].metadata;
                if (availableSubQueryMetadata.numberOfColumnsReturned > 1) {
                    throw Error(`The sub query, ${subQueryName}, should only return 1 column`);
                }

                // If the sub query could return more than 1 row, check that the operator is IN or NOT IN. 
                if (availableSubQueryMetadata.maxNumberOfRowsReturned > 1) {
                    if (criterion.operator.toLowerCase() !== 'in' && criterion.operator.toLowerCase() !== 'notin') {
                        throw Error(`Sub query, ${subQueryName}, could return more than 1 row but the criterion operator is not IN or NOT IN`);
                    }
                }

                // If the criterion's column's data type is numeric, check that the sub query data type is also numeric.
                
                // let onlyReturnColumns = matchingSubQuery.onlyReturnColumns; // Check that this is only 1 item in array for subqueries. 
                let metadataColumn = availableSubQueryMetadata.columns[0];
                let metadaColumnJdbcType = getJdbcSqlType(metadataColumn.dataType);
                if (NUMERIC_DATA_TYPES.includes(criterionColumnJdbcDataType) && ! NUMERIC_DATA_TYPES.includes(metadaColumnJdbcType)) {
                    throw Error(`The column, ${criterion.column.fullyQualifiedName}, is a(n) ${criterionColumnJdbcDataType}, but the sub query column, 
                        ${metadataColumn.fullyQualifiedName}, is a(n) ${metadaColumnJdbcType}`)
                }
            }
        });
    })
}

export const assertAllValidations = () => {
    try{
        assertDatabaseIsSelected();
        assertSchemasAreSelected();
        assertTablesAreSelected();
    } catch (e) {
        return new UiMessage('schemasAndTables', e.message);
    }

    try {
        assertSubQueriesAreCorrect();
    } catch (e) {
        return new UiMessage('subQueries', e.message);
    }

    try {
        assertJoinsExist();
    } catch (e) {
        return new UiMessage('joins', e.message);
    }

    try {
        assertColumnsAreSelected();
    } catch (e) {
        return new UiMessage('columns', e.message);
    }

    try {
        assertCriteriaOperatorsAreCorrect();
    } catch (e) {
        return new UiMessage('criteria', e.message);
    }

    try {
        assertCriteriaFiltersAreCorrect();
    } catch (e) {
        return new UiMessage('criteria', e.message);
    }

    return null;
};
