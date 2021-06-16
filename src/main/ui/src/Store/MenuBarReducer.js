import * as Constants from '../Config/Constants';

const initialState = {
    queryTemplates: {
        isHidden: true,
        uiMessages: [],
        isValid: true,
        hasUnsavedChanges: false
    },
    schemasAndTables: {
        isHidden: false,
        uiMessages: [],  // Default message when app loads and database has not been chosen.
        isValid: true,
        hasUnsavedChanges: false
    },
    joins: {
        isHidden: true,
        uiMessages: [],
        isValid: true,  // If the number of joins is 1 less than the number of tables chosen, then false.
        hasUnsavedChanges: false
    },
    columns: {
        isHidden: true,
        uiMessages: [],
        isValid: false, // False until columns are selected.
        hasUnsavedChanges: false
    },
    criteria: {
        isHidden: true,
        uiMessages: [],
        isValid: true,
        hasUnsavedChanges: false
    },
    otherOptions: {
        isHidden: true,
        uiMessages: [],
        isValid: true,
        hasUnsavedChanges: false
    },
    subQueries: {
        isHidden: true,
        uiMessages: [],
        isValid: true,
        hasUnsavedChanges: false
    }
};

const hideAllElements = (state) => {
    for (let key in state) {
        state[key].isHidden = true;
    }
};

const clearSectionUiMessages = (state) => {
    for (let section in state) {
        state[section].uiMessages = [];
    }
};

const resetSectionValidity = (state) => {
    for (let section in state) {
        state[section].isValid = true;
    }
};

const updateSectionUiMessages = (state, uiMessage) => {
    clearSectionUiMessages(state);
    resetSectionValidity(state);

    if (uiMessage !== null) {
        let section = uiMessage.section;

        // Add section UI message.
        state[section].uiMessages.push(
            uiMessage.message
        );

        // Set section validity to false because it has a UI message.
        state[section].isValid = false;
    }
};

const menuBarReducer = (state = initialState, action) => {
    // Copy state and show all elements (set all keys' value to true).
    let newState = JSON.parse(JSON.stringify(state));

    switch (action.type) {
        case Constants.SUB_QUERIES:
            hideAllElements(newState);
            newState.subQueries.isHidden = false;
            return newState;
        case Constants.JOINS:
            hideAllElements(newState);
            newState.joins.isHidden = false;
            return newState;
        case Constants.SCHEMAS_AND_TABLES:
            hideAllElements(newState);
            newState.schemasAndTables.isHidden = false;
            return newState;
        case Constants.COLUMNS:
            hideAllElements(newState);
            newState.columns.isHidden = false;
            return newState;
        case Constants.CRITERIA:
            hideAllElements(newState);
            newState.criteria.isHidden = false;
            return newState;
        case Constants.OTHER_OPTIONS:
            hideAllElements(newState);
            newState.otherOptions.isHidden = false;
            return newState;
        case Constants.QUERY_TEMPLATES:
            hideAllElements(newState);
            newState.queryTemplates.isHidden = false;
            return newState;
        case 'UPDATE_UI_MESSAGES':
            updateSectionUiMessages(newState, action.payload.uiMessages);
            return newState;
        default:
            return state;
    }

};

export default menuBarReducer;
