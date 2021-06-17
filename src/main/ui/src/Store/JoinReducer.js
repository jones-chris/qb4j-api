import * as Constants from "../Config/Constants";
import cloneDeep from 'lodash/cloneDeep';
import * as Utils from "../Utils/Utils";


const initialState = {
    joins: []
};

const joinReducer = (state = initialState, action) => {
    // Deep clone the state's `joins`.
    let newJoins = cloneDeep(state.joins);

    switch (action.type) {
        case 'ADD_JOIN':
            // Create a new join object and add it to the state's `joins` item.
            newJoins.push({
                joinType: Constants.JOIN_IMAGES[0].name,
                parentTable: action.payload.parentTable,
                targetTable: action.payload.targetTable,
                parentJoinColumns: action.payload.parentJoinColumns,
                targetJoinColumns: action.payload.targetJoinColumns,
                metadata: {
                    id: state.joins.length,
                    joinImageUrl: Constants.JOIN_IMAGES[0].image,
                    availableColumns: {
                        parentColumns: action.payload.availableParentColumns,
                        targetColumns: action.payload.availableTargetColumns
                    }
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        case 'DELETE_JOIN':
            let joinId = parseInt(action.payload.joinId);

            // Create new array of joins that excludes the id being deleted.
            newJoins = newJoins.filter(join => join.metadata.id !== joinId);

            // Renumber join ids.
            newJoins.forEach((join, index) => join.metadata.id = index);

            return {
                ...state,
                joins: newJoins
            };
        case 'CHANGE_JOIN_TYPE':
            // Get next join image based on currentJoinType.
            joinId = parseInt(action.payload.joinId);
            let currentJoinType = state.joins.find(join => join.metadata.id === joinId).joinType;

            let currentJoinTypeIndex;
            for (let i=0; i<Constants.JOIN_IMAGES.length; i++) {
                let join = Constants.JOIN_IMAGES[i];
                if (join.name === currentJoinType) {
                    currentJoinTypeIndex = i;
                    break;
                }
            }

            let nextJoinType;
            let nextJoinImageUrl;
            try {
                nextJoinType = Constants.JOIN_IMAGES[currentJoinTypeIndex + 1].name;
                nextJoinImageUrl = Constants.JOIN_IMAGES[currentJoinTypeIndex + 1].image;
            } catch (e) {
                nextJoinType = Constants.JOIN_IMAGES[0].name; // Default to first item in case of index out of range exception.
                nextJoinImageUrl = Constants.JOIN_IMAGES[0].image; // Default to first item in case of index out of range exception.
            }

            // Copy state
            newJoins.forEach(join => {
                if (join.metadata.id === joinId) {
                    join.joinType = nextJoinType;
                    join.metadata.joinImageUrl = nextJoinImageUrl;
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        case 'CHANGE_TABLE':
            let parentTableName = Utils.getSelectedOptions(document.getElementById(action.payload.parentTableElementName))[0];
            let parentTableObject = action.payload.availableTables.find(table => { return table.fullyQualifiedName === parentTableName; });

            let targetTableName = Utils.getSelectedOptions(document.getElementById(action.payload.targetTableElementName))[0];
            let targetTableObject = action.payload.availableTables.find(table => { return table.fullyQualifiedName === targetTableName; });

            newJoins.forEach(join => {
                if (join.metadata.id === action.payload.joinId) {

                    // Update join's parent and target tables.
                    join.parentTable = parentTableObject;
                    join.targetTable = targetTableObject;

                    if (action.payload.availableColumns.length !== 0) {
                        // Update join's parentJoinColumns and targetJoinColumns
                        if (join.parentJoinColumns.length === 0) {
                            join.parentJoinColumns.push(action.payload.availableColumns.find(column => {  // find() because we just need first item that meets criteria.
                                return column.databaseName === parentTableObject.databaseName && column.schemaName === parentTableObject.schemaName && column.tableName === parentTableObject.tableName;
                            }));
                        }

                        if (join.targetJoinColumns.length === 0) {
                            join.targetJoinColumns.push(action.payload.availableColumns.find(column => {  // find() because we just need first item that meets criteria.
                                return column.databaseName === targetTableObject.databaseName && column.schemaName === targetTableObject.schemaName && column.tableName === targetTableObject.tableName;
                            }));
                        }

                        // Get available parent columns for this join.
                        join.metadata.availableColumns.parentColumns = action.payload.availableColumns.filter(column => {  // filter() because we need all items that meets criteria.
                            return column.databaseName === parentTableObject.databaseName && column.schemaName === parentTableObject.schemaName && column.tableName === parentTableObject.tableName;
                        });

                        // Get available target columns for this join.
                        join.metadata.availableColumns.targetColumns = action.payload.availableColumns.filter(column => {  // filter() because we need all items that meets criteria.
                            return column.databaseName === targetTableObject.databaseName && column.schemaName === targetTableObject.schemaName && column.tableName === targetTableObject.tableName;
                        });
                    }
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        case 'CHANGE_COLUMN':
            let parentColumnEl = document.getElementById(action.payload.parentJoinColumnsElementId);
            let index = parseInt(parentColumnEl.getAttribute('data-index'));

            let parentColumn = Utils.getSelectedOptions(parentColumnEl)[0];
            let parentColumnObject = action.payload.availableColumns.find(column => { return column.fullyQualifiedName === parentColumn; });

            let targetColumn = Utils.getSelectedOptions(document.getElementById(action.payload.targetJoinColumnsElementId))[0];
            let targetColumnObject = action.payload.availableColumns.find(column => { return column.fullyQualifiedName === targetColumn; });

            newJoins.forEach(join => {
                if (join.metadata.id === action.payload.joinId) {
                    join.parentJoinColumns.splice(index, 1, parentColumnObject);
                    join.targetJoinColumns.splice(index, 1, targetColumnObject);
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        case 'ADD_JOIN_COLUMN':
            newJoins.forEach(join => {
                // Get first available parent column and first available target column.
                let firstAvailableParentColumn = join.parentJoinColumns[0];
                let firstAvailableTargetColumn = join.targetJoinColumns[0];

                // Add first available parent column and first available target column as another item at the end of the array.
                if (join.metadata.id === action.payload.joinId) {
                    join.parentJoinColumns.push(firstAvailableParentColumn);
                    join.targetJoinColumns.push(firstAvailableTargetColumn);
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        case 'DELETE_JOIN_COLUMN':
            newJoins.forEach(join => {
                if (join.metadata.id === action.payload.joinId) {
                    // Remove the parent join column and target join column at the joinColumnIndex.
                    join.parentJoinColumns.splice(action.payload.joinColumnIndex, 1);
                    join.targetJoinColumns.splice(action.payload.joinColumnIndex, 1)
                }
            });

            return {
                ...state,
                joins: newJoins
            };
        default:
            return state;
    }
};

export default joinReducer;
