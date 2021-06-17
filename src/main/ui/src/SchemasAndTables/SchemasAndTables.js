import React, { Component } from 'react';
import './SchemasAndTables.css';
import { connect } from "react-redux";
import { store } from '../index';
import * as Utils from "../Utils/Utils";
import { assertAllValidations } from "../Validators/Validators";


class SchemasAndTables extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        // Create JSX list of all available schemas so that it can be used when rendering.
        const availableSchemas = [];
        this.props.availableSchemas.forEach(schema => {
            availableSchemas.push(
                <option key={schema.fullyQualifiedName}
                        value={schema.fullyQualifiedName}
                        selected={this.props.selectedSchemas.includes(schema)}
                >
                    {schema.schemaName}
                </option>
            )
        });

        // Create JSX list of all available tables so that it can be used when rendering.
        const availableTables = [];
        if (this.props.availableTables) {
            // Get selected tables...the map() function was returning an empty array so I have to do it the verbose way.
            let selectedTables = [];
            for (let i=0; i<this.props.selectedTables.length; i++) {
                let selectedTable = this.props.selectedTables[i];
                let fullyQualifiedName = selectedTable.fullyQualifiedName;
                selectedTables.push(fullyQualifiedName);
            }

            // For each available table, create the JSX.
            this.props.availableTables.forEach(table => {
                let isSelected = selectedTables.includes(table.fullyQualifiedName);

                let optionDisplayText;
                if (table.schemaName === "null") {
                    optionDisplayText = table.tableName;
                } else {
                    optionDisplayText = `${table.schemaName}.${table.tableName}`
                }

                availableTables.push(
                    <option key={table.fullyQualifiedName}
                            value={table.fullyQualifiedName}
                            selected={isSelected}
                    >
                        {optionDisplayText}
                    </option>
                );
            })
        }

        return (
            <div>
                <div id="schemasDiv" className="schemas-div" hidden={this.props.hidden === 'true'}>
                    <label htmlFor="schemas">Schemas</label>

                    <select id="schemas" size="20" multiple={true}
                            onChange={(event) => this.props.onSelectSchemaHandler(event.target)}
                    >
                        {availableSchemas}
                    </select>
                </div>

                <div id="tablesDiv" className="tables-div" hidden={this.props.hidden === 'true'}>
                    <label htmlFor="table">Tables</label>
                    <select id="table" name="table" multiple={true} size="20"
                            onChange={(event) => this.props.onSelectTableHandler(event.target)}
                    >
                        {availableTables}
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
        onSelectSchemaHandler: (target) => {
            let newSelectedSchemasFullyQualifiedNames = Utils.getSelectedOptions(target);

            // Get schema object that has been selected.
            let selectedSchemaObjects = store.getState().query.availableSchemas.filter(schema => newSelectedSchemasFullyQualifiedNames.includes(schema.fullyQualifiedName));

            // Create a string with the schema names joined together with `&` to be used in API call.
            let joinedSchemaString = selectedSchemaObjects.map(schema => schema.schemaName).join('&');

            let selectedDatabaseName = store.getState().query.selectedDatabase.databaseName;
            let apiUrl = `${window.location.origin}/metadata/${selectedDatabaseName}/${joinedSchemaString}/table-and-view`;
            fetch(apiUrl)
                .then(response => response.json())
                .then(tables => {
                    console.log(tables);

                    dispatch({
                        type: 'SELECT_SCHEMA',
                        payload: {
                            selectedSchemas: selectedSchemaObjects,
                            tables: tables
                        }
                    });

                    dispatch({
                        type: 'UPDATE_UI_MESSAGES',
                        payload: {
                            uiMessages: assertAllValidations()
                        }
                    })
                });
        },
        onSelectTableHandler: (target) => {
            let newSelectedTableFullyQualifiedNames = Utils.getSelectedOptions(target);

            // Get the table object for the table that was selected.
            let allTables = store.getState().query.availableTables.filter(table => newSelectedTableFullyQualifiedNames.includes(table.fullyQualifiedName));

            // Get table columns for all selected tables.
            let apiUrl = `${window.location.origin}/metadata/database/schema/table/column`;
            fetch(apiUrl,{
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(allTables)
            }).then(response => response.json())
                .then(columns => {
                    console.log(columns);

                    dispatch({
                        type: 'SELECT_TABLE',
                        payload: {
                            selectedTables: allTables,
                            availableColumns: columns
                        }
                    });

                    // Now that state has been updated, run validations, and update UI messages.
                    dispatch({
                        type: 'UPDATE_UI_MESSAGES',
                        payload: {
                            uiMessages: assertAllValidations()
                        }
                    })
                });
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(SchemasAndTables);
