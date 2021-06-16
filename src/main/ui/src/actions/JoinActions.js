import {store} from "../index";

export const addJoin = () => {
    let queryState = store.getState().query;

    // Get the default table and columns.
    let defaultTable = (queryState.availableTables.length > 0) ? queryState.availableTables[0] : '';

    let defaultAvailableColumns = (queryState.availableTables.length > 0)
        ? queryState.availableColumns.filter(column => {
            return column.databaseName === defaultTable.databaseName
                && column.schemaName === defaultTable.schemaName
                && column.tableName === defaultTable.tableName;
        })
        : [];

    return {
        type: 'ADD_JOIN',
        payload: {
            availableTables: store.getState().query.availableTables,
            parentTable: defaultTable,
            targetTable: defaultTable,
            parentJoinColumns: [],
            targetJoinColumns: [],
            availableParentColumns: defaultAvailableColumns,
            availableTargetColumns: defaultAvailableColumns
        }
    };
};

export const deleteJoin = (joinId) => {
    return {
        type: 'DELETE_JOIN',
        payload: {
            joinId: joinId
        }
    }
};

export const changeJoinType = (joinId) => {
    return {
        type: 'CHANGE_JOIN_TYPE',
        payload: {
            joinId: joinId
        }
    }
};

export const changeTable = (joinId, parentTableElementName, targetTableElementName) => {
    return {
        type: 'CHANGE_TABLE',
        payload: {
            joinId: joinId,
            parentTableElementName: parentTableElementName,
            targetTableElementName: targetTableElementName,
            availableTables: store.getState().query.selectedTables,
            availableColumns: store.getState().query.availableColumns
        }
    }
};

export const changeColumn = (joinId, parentJoinColumnsElementId, targetJoinColumnsElementId) => {
    return {
        type: 'CHANGE_COLUMN',
        payload: {
            joinId: joinId,
            parentJoinColumnsElementId: parentJoinColumnsElementId,
            targetJoinColumnsElementId: targetJoinColumnsElementId,
            availableColumns: store.getState().query.availableColumns
        }
    }
};

export const addJoinColumn = (joinId) => {
    return {
        type: 'ADD_JOIN_COLUMN',
        payload: {
            joinId: joinId
        }
    }
};

export const deleteJoinColumn = (joinId, joinColumnIndex) => {
    return {
        type: 'DELETE_JOIN_COLUMN',
        payload: {
            joinId: joinId,
            joinColumnIndex: joinColumnIndex
        }
    }
};

/**
 * Returns an array of the joins with each join's metadata removed.
 *
 * @param joins The array of joins to remove the metadata attribute from.
 * @returns An array of joins with the metadata removed.
 */
export const removeJoinMetadata = (joins) => {
    return joins.map(join => {
        join = Object.assign({}, join);
        delete join.metadata;
        return join;
    })
};


