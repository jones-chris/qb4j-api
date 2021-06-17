import * as React from "react";
import './Columns.css';
import * as Utils from '../Utils/Utils';
import { connect } from "react-redux";
import { store } from '../index';
import {assertAllValidations} from "../Validators/Validators";

class Columns extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        // Create JSX list of all available columns so that it can be used when rendering.
        const availableColumns = [];
        if (this.props.availableColumns) {
            this.props.availableColumns.forEach(column => {
                let optionDisplayText;
                if (column.schemaName === "null") {
                    optionDisplayText = `${column.tableName}.${column.columnName} (${Utils.getJdbcSqlType(column.dataType)})`;
                } else {
                    optionDisplayText = `${column.schemaName}.${column.tableName}.${column.columnName} (${Utils.getJdbcSqlType(column.dataType)})`;
                }

                availableColumns.push(
                    <option key={column.fullyQualifiedName}
                            value={column.fullyQualifiedName}>
                        {optionDisplayText}
                    </option>
                );
            })
        }

        // Create JSX list of all selected columns so that it can be used when rendering.
        const selectedColumns = [];
        if (this.props.selectedColumns) {
            this.props.selectedColumns.forEach(column => {
                let optionDisplayText;
                if (column.schemaName === "null") {
                    optionDisplayText = `${column.tableName}.${column.columnName} (${Utils.getJdbcSqlType(column.dataType)})`;
                } else {
                    optionDisplayText = `${column.schemaName}.${column.tableName}.${column.columnName} (${Utils.getJdbcSqlType(column.dataType)})`;
                }

                selectedColumns.push(
                    <option key={column.fullyQualifiedName}
                            value={column.fullyQualifiedName}>
                        {optionDisplayText}
                    </option>
                );
            })
        }

        return (
            <div id="tableColumns" className="table-columns"
                 hidden={this.props.hidden === 'true'}
            >
                <div id="availableColumnsDiv">
                    <label htmlFor="availableColumns">Table Columns</label>
                    <select id="availableColumns" name="availableColumns" multiple size="20">
                        {availableColumns}
                    </select>
                </div>

                <div id="addRemoveColumns" className="available-columns-buttons-div">
                    <button id="addColumnsButton" name="addColumnsButton" type="button"
                            className="available-columns-add-button"
                            onClick={this.props.onAddSelectedColumnHandler}
                    >&#8594;</button>

                    <br/>

                    <button id="removeColumnsButton" name="removeColumnsButton" type="button"
                            className="available-columns-remove-button"
                            onClick={this.props.onRemoveSelectedColumnHandler}
                    >&#8592;</button>
                </div>

                <div id="selectedColumnsDiv">
                    <label htmlFor="selectedColumns">Selected Columns</label>
                    <select id="columns" name="columns" multiple size="20">
                        {selectedColumns}
                    </select>
                </div>
            </div>
        );
    }

}

const mapReduxStateToProps = (reduxState) => {
    return reduxState.query;
};

const mapDispatchToProps = (dispatch) => {
    return {
        onAddSelectedColumnHandler: () => {
            let availableColumnsSelectElement = document.getElementById('availableColumns');
            let newSelectedColumnsFullyQualifiedNames = Utils.getSelectedOptions(availableColumnsSelectElement);

            // Get the column JSON object based on newSelectedColumns, which is an array of the column fullyQualifiedNames.
            let newSelectedColumns = store.getState().query.availableColumns.filter(column => {
                return newSelectedColumnsFullyQualifiedNames.includes(column.fullyQualifiedName);
            });

            dispatch({
                type: 'ADD_SELECTED_COLUMN',
                payload: {
                    selectedColumns: newSelectedColumns
                }
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        onRemoveSelectedColumnHandler: () => {
            let selectedColumnsSelectElement = document.getElementById('columns');
            let fullyQualifiedColumnsNamesToRemove = Utils.getSelectedOptions(selectedColumnsSelectElement);

            let newSelectedColumns = store.getState().query.selectedColumns.filter(column => {
                return ! fullyQualifiedColumnsNamesToRemove.includes(column.fullyQualifiedName);
            });

            dispatch({
                type: 'REMOVE_SELECTED_COLUMN',
                payload: {
                    selectedColumns: newSelectedColumns
                }
            });

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            })
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(Columns);
