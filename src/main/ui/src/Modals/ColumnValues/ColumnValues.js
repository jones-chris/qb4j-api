import React from "react";
import './ColumnValues.css';
import {store} from "../../index";
import {connect} from "react-redux";
import * as Utils from "../../Utils/Utils";
import _ from 'lodash';
import {assertAllValidations} from "../../Validators/Validators";
import {flattenCriteria} from "../../actions/CriteriaActions";

class ColumnValues extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        // Create available column values JSX.
        let availableColumnValuesJsx = [];
        if (this.props.columnValueModal !== null) {
            this.props.columnValueModal.availableColumnValues.forEach((availableColumnValue, index) => {
                availableColumnValuesJsx.push(
                    <option key={index} value={availableColumnValue}>{availableColumnValue}</option>
                )
            });
        }

        // Create selected column values JSX.
        let selectedColumnValuesJsx = [];
        if (this.props.columnValueModal !== null) {
            this.props.columnValueModal.selectedColumnValues.forEach((selectedColumnValue, index) => {
                selectedColumnValuesJsx.push(
                    <option key={index} value={selectedColumnValue}>{selectedColumnValue}</option>
                )
            })
        }

        return (
            <div hidden={this.props.hidden === 'true'} className="column-members-modal">

                <div id="columnMembersModalContent" className="column-members-modal-content">

                    <button type="button" className="column-members-modal-close-button"
                        onClick={this.props.onCloseColumnValues}
                    >
                        X
                    </button>

                    <br/>

                    {/*Search box area*/}
                    <div className="column-members-modal-search">
                        <label htmlFor="search">Search</label>
                        <input type="text" placeholder="ex: Cap%" autoFocus="autofocus"
                               disabled={(this.props.columnValueModal !==  null) ? this.props.columnValueModal.firstPaginationOccurred : false}
                               onChange={(event) => this.props.onSearchChange(event.target.value)}
                        />
                    </div>

                    {/*Limit and paging area*/}
                    <div className="column-members-modal-pagination">
                        <input type="text" hidden="hidden" defaultValue="0"/>

                        <label htmlFor="columnMembersLimit">Limit</label>
                        <select onChange={(event) => this.props.onLimitChange(event.target.value)}
                                disabled={(this.props.columnValueModal !==  null) ? this.props.columnValueModal.firstPaginationOccurred : false}
                        >
                            <option value="2">2</option>
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </select>

                        <label htmlFor="columnMembersAscending">Ordering</label>
                        <select onChange={(event) => this.props.onAscendingChange(event.target.value)}
                                disabled={(this.props.columnValueModal !==  null) ? this.props.columnValueModal.firstPaginationOccurred : false}
                        >
                            <option value="true">A-Z,1-9</option>
                            <option value="false">Z-A,9-1</option>
                        </select>

                        <br/>
                        <br/>

                        <p className={(this.props.columnValueModal !== null && this.props.columnValueModal.uiMessage !== '') ? 'warning' : null}>
                            {(this.props.columnValueModal !== null) ? this.props.columnValueModal.uiMessage : ''}
                        </p>

                        <button type="button"
                                disabled={(this.props.columnValueModal !== null) ? this.props.columnValueModal.disablePriorPageButton : true }
                                onClick={this.props.onPriorPage}
                        >
                            Prior Page
                        </button>

                        <button type="button"
                                disabled={(this.props.columnValueModal !== null) ? this.props.columnValueModal.disableNextPageButton : false }
                                onClick={this.props.onNextPage}
                        >
                            Next Page
                        </button>
                    </div>

                    {/*Member selection area*/}
                    <div className='column-members-selection'>

                        <div className="column-members-modal-available-members">
                            <label htmlFor="availableValues">Available Column Members</label>
                            <select id="availableColumnValues" multiple={true} size="20">
                                {availableColumnValuesJsx}
                            </select>
                        </div>

                        <div className="column-members-selection-button-div">
                            <div>
                                <button type="button" className="column-members-selection-button"
                                        onClick={this.props.onAddSelectColumnValues}
                                >
                                    →
                                </button>
                            </div>

                            <br/>

                            <div>
                                <button type="button" className="column-members-selection-button"
                                        onClick={this.props.onRemoveSelectedColumnValues}
                                >
                                    &#8592;
                                </button>
                            </div>
                        </div>

                        <div className="column-members-modal-selected-members">
                            <label htmlFor="selectedMembers">Selected Column Members</label>
                            <select id="selectedColumnValues" multiple={true} size="20">
                                {selectedColumnValuesJsx}
                            </select>
                        </div>

                    </div>

                    {/*Submission and Cancel button area*/}
                    <div className="column-members-modal-submit">
                        <input type="button" value="OK"
                               onClick={this.props.onSubmitColumnValues}
                        />

                        <button type="button"
                            onClick={this.props.onCloseColumnValues}
                        >
                            Cancel
                        </button>
                    </div>

                </div>

            </div>
        );
    }

}

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.query,
        ...reduxState.modal
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
        onSubmitColumnValues: () => {
            // Get object ref and attribute to update.
            let target = store.getState().modal.columnValueModal.target;
            let targetObjectId = target.objectId;

            // Get the selected column values that will become the value of the target object ref and target attribute.
            let selectedColumnValues = store.getState().modal.columnValueModal.selectedColumnValues;

            if (target.type === 'CRITERIA') {
                // Copy the query state's criteria.
                let newCriteria = [...store.getState().query.criteria];

                let flattenedCriteria = flattenCriteria(newCriteria, []);
                flattenedCriteria.forEach(criterion => {
                    if (criterion.metadata.id === targetObjectId) {
                        criterion.filter.values = selectedColumnValues;
                    }
                });

                // Dispatch action to update criteria.
                dispatch({
                    type: 'UPDATE_COLUMN_VALUES_MODAL_TARGET',
                    payload: {
                        newCriteria: newCriteria
                    }
                });
            }
            else if (target.type === 'SUBQUERY') {
                let newSubQueries = [...store.getState().query.subQueries];

                newSubQueries.forEach(subQuery => {
                    if (subQuery.id === targetObjectId) {
                        subQuery.parametersAndArguments[target.parameterName] = selectedColumnValues;
                    }
                })
                
                dispatch({
                    type: 'UPDATE_SUBQUERY_PARAMETER_COLUMN_VALUES',
                    payload: {
                        newSubQueries: newSubQueries
                    }
                });
            }

            // Dispatch action to close Column Values modal.
            dispatch({
                type: 'CLOSE_COLUMN_VALUES_MODAL',
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onCloseColumnValues: () => {
            dispatch({
                type: 'CLOSE_COLUMN_VALUES_MODAL',
            });
        },
        onLimitChange: (limit) => {
            dispatch({
                type: 'UPDATE_COLUMN_VALUES_LIMIT',
                payload: {
                    newLimit: limit
                }
            })
        },
        onAscendingChange: (ascending) => {
            dispatch({
                type: 'UPDATE_COLUMN_VALUES_ASCENDING',
                payload: {
                    newAscending: ascending
                }
            })
        },
        onSearchChange: (search) => {
            dispatch({
                type: 'UPDATE_COLUMN_VALUES_SEARCH',
                payload: {
                    newSearch: search
                }
            })
        },
        onNextPage: () => {
            let columnValueModalState = store.getState().modal.columnValueModal;
            let column = columnValueModalState.target.column;
            let databaseName = column.databaseName;
            let schemaName = column.schemaName;
            let tableName = column.tableName;
            let columnName = column.columnName;

            let baseApiUrl = `${window.location.origin}/data/${databaseName}/${schemaName}/${tableName}/${columnName}/column-member`;
            let queryParams = `?limit=${columnValueModalState.limit}&offset=${columnValueModalState.offset}&ascending=${columnValueModalState.ascending}`;

            // Concatenate the search text if it exists.
            if (columnValueModalState.search !== null) {
                queryParams += `&search=${columnValueModalState.search}`
            }

            let fullUrl = baseApiUrl + queryParams;

            fetch(fullUrl)
                .then(response => response.json())
                .then(columnValues => {
                    console.log(columnValues);

                    // Default to the current availableColumnValues in case there is no data in the response body.
                    let newColumnValues = columnValueModalState.availableColumnValues;
                    let uiMessage = '';
                    let disableNextPageButton = false;
                    let offsetDelta = columnValueModalState.limit;
                    if (columnValues.data.length > 0) {
                        newColumnValues = columnValues.data.flatMap(row => [row[0]]);
                    } else {
                        uiMessage = 'There are no more column values to retrieve';
                        disableNextPageButton = true;
                        offsetDelta = 0;  // If there are no more column values to retrieve, then don't increase/decrease the offset.
                    }

                    dispatch({
                        type: 'UPDATE_AVAILABLE_COLUMN_MEMBERS',
                        payload: {
                            newColumnValues: newColumnValues,
                            uiMessage: uiMessage,
                            disablePriorPageButton: false,
                            disableNextPageButton: disableNextPageButton,
                            offsetDelta: offsetDelta
                        }
                    });
                })
        },
        onPriorPage: () => {
            let columnValueModalState = store.getState().modal.columnValueModal;
            let column = columnValueModalState.target.column;
            let databaseName = column.databaseName;
            let schemaName = column.schemaName;
            let tableName = column.tableName;
            let columnName = column.columnName;

            let newOffset = columnValueModalState.offset - columnValueModalState.limit;

            let baseApiUrl = `${window.location.origin}/data/${databaseName}/${schemaName}/${tableName}/${columnName}/column-member`;
            let queryParams = `?limit=${columnValueModalState.limit}&offset=${newOffset}&ascending=${columnValueModalState.ascending}`;

            // Concatenate the search text if it exists.
            if (columnValueModalState.search !== null) {
                queryParams += `&search=${columnValueModalState.search}`
            }

            let fullUrl = baseApiUrl + queryParams;

            fetch(fullUrl)
                .then(response => response.json())
                .then(columnValues => {
                    console.log(columnValues);

                    // Default to the current availableColumnValues if the offset is now 0 - meaning we are at the first page.
                    let newColumnValues = columnValueModalState.availableColumnValues;
                    let uiMessage = '';

                    let disablePriorPageButton = newOffset === 0;  // Disable the prior page if offset is 0.
                    let disableNextPageButton = false;  // When getting the prior page, the Next Page button is always enabled.

                    let offsetDelta = (- columnValueModalState.limit);
                    newColumnValues = columnValues.data.flatMap(row => [row[0]]);

                    dispatch({
                        type: 'UPDATE_AVAILABLE_COLUMN_MEMBERS',
                        payload: {
                            newColumnValues: newColumnValues,
                            uiMessage: uiMessage,
                            disablePriorPageButton: disablePriorPageButton,
                            disableNextPageButton: disableNextPageButton,
                            offsetDelta: offsetDelta
                        }
                    });
                })
        },
        onAddSelectColumnValues: () => {
            let availableColumnValuesElement = document.getElementById('availableColumnValues');
            let selectedAvailableColumnValues = Utils.getSelectedOptions(availableColumnValuesElement);

            // Remove selected available column values that already exist in the `selectedColumnValues` state so that
            // duplicates are prevented.
            selectedAvailableColumnValues = selectedAvailableColumnValues.filter(columnValue => {
                return ! store.getState().modal.columnValueModal.selectedColumnValues.includes(columnValue);
            });

            dispatch({
                type: 'ADD_SELECTED_COLUMN_VALUES',
                payload: {
                    columnValuesToAdd: selectedAvailableColumnValues
                }
            })
        },
        onRemoveSelectedColumnValues: () => {
            let selectedColumnValuesElement = document.getElementById('selectedColumnValues');
            let columnValuesToRemove = Utils.getSelectedOptions(selectedColumnValuesElement);

            dispatch({
                type: 'REMOVE_SELECTED_COLUMN_VALUES',
                payload: {
                    columnValuesToRemove: columnValuesToRemove
                }
            })
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(ColumnValues);
