const initialState = {
    metadata: {
        name: '',
        description: '',
        version: 0,
        author: '',
        isDiscoverable: false
    },
    availableDatabases: [],
    selectedDatabase: null,
    availableSchemas: [],
    selectedSchemas: [],
    availableTables: [],
    selectedTables: [],
    availableColumns: [],
    selectedColumns: [],
    criteria: [],
    joins: [],
    distinct: false,
    suppressNulls: false,
    limit: 10,
    offset: 0,
    ascending: false,
    availableSubQueries: {},
    subQueries: []
};

const queryReducer = (state = initialState, action) => {
    let newState = {...state};

    switch (action.type) {
        case 'UPDATE_AVAILABLE_DATABASES':
            newState.availableDatabases = action.payload.availableDatabases;
            return newState;
        case 'CHANGE_SELECTED_DATABASE':
            newState.availableDatabases = state.availableDatabases;
            newState.selectedDatabase = action.payload.selectedDatabase;
            return newState;
        case 'UPDATE_AVAILABLE_SCHEMAS':
            newState.availableSchemas = action.payload.availableSchemas;
            return newState;
        case 'SELECT_SCHEMA':
            newState.selectedSchemas = action.payload.selectedSchemas;
            newState.availableTables = action.payload.tables;
            return newState;
        case 'SELECT_TABLE':
            newState.selectedTables = action.payload.selectedTables;
            newState.availableColumns = action.payload.availableColumns;
            return newState;
        case 'ADD_SELECTED_COLUMN':
            newState.selectedColumns = action.payload.selectedColumns;
            return newState;
        case 'REMOVE_SELECTED_COLUMN':
            newState.selectedColumns = action.payload.selectedColumns;
            return newState;
        case 'UPDATE_DISTINCT':
            newState.distinct = ! state.distinct;
            return newState;
        case 'UPDATE_SUPPRESS_NULLS':
            newState.suppressNulls = ! state.suppressNulls;
            return newState;
        case 'UPDATE_LIMIT':
            newState.limit = action.payload.newLimit;
            return newState;
        case 'UPDATE_OFFSET':
            newState.offset = action.payload.newOffset;
            return newState;
        case 'ADD_CRITERIA':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_CRITERIA':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_COLUMN_VALUES_MODAL_TARGET':
            newState.criteria = action.payload.newCriteria;
            return newState;
        case 'UPDATE_SUBQUERY_PARAMETER_COLUMN_VALUES':
            newState.subQueries = action.payload.newSubQueries;
            return newState;
        case 'IMPORT_QUERY_TEMPLATE':
            let queryTemplate = action.payload.queryTemplate;

            newState.selectedColumns = queryTemplate.columns; //todo:  finish this once qb4j lib's data models are corrected to match web app and front end. 
            return newState;
        case 'UPDATE_QUERY_METADATA':
            let attributeNameToUpdate = action.payload.attribute;
            let value = action.payload.value;

            newState.metadata[attributeNameToUpdate] = value;
            return newState;
        case 'UPDATE_AVAIALABLE_SUBQUERIES':
            newState.availableSubQueries = action.payload.availableSubQueries;
            return newState;
        case 'UPDATE_SUBQUERIES':
            newState.subQueries = action.payload.subQueries;
            return newState;
        default:
            return state;
    }
};

export default queryReducer;
