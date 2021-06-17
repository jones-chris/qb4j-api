import React, { Component } from 'react';
import './MenuBar.css';
import * as Constants from '../Config/Constants';
import { connect } from 'react-redux'
import { store } from "../index";
import Navbar from "react-bootstrap/Navbar";
import Nav from "react-bootstrap/Nav";
import NavDropdown from "react-bootstrap/NavDropdown";
import Button from "react-bootstrap/Button";
import { runQuery } from "../actions/QueryActions";
import { assertAllValidations } from "../Validators/Validators";


class MenuBar extends Component {

    constructor(props) {
        super(props);

        // Get target databases so they can be added to the drop down nav bar.
        let apiUrl = `${window.location.origin}/metadata/database`;
        fetch(apiUrl)
            .then(response => {
                if (! response.ok) {
                    throw Error('Could not retrieve databases')
                }

                return response.json()
            }).then(databases => {
                console.log(databases);
                this.props.updateAvailableDatabases(databases);
            })
            .catch(reason => {
                console.log(reason);
            });
    }

    onRunQueryHandler = () => {
        // Check if query is valid first.
        const currentMenuBarState = store.getState().menuBar;
        for (let section in currentMenuBarState) {
            if (! currentMenuBarState[section].isValid) {
                alert('Resolve all messages before running the query');
                return;
            }
        }

        runQuery();
    };

    render() {
        // Create database NavDropdown.Item JSX.
        let availableDatabases = [];
        store.getState().query.availableDatabases.forEach(database => {
            availableDatabases.push(
                <NavDropdown.Item key={database.databaseName}
                                  onClick={() => this.props.onChangeSelectedDatabase(database)}
                >
                    {database.databaseName} ({database.databaseType})
                </NavDropdown.Item>
            );
        });

        return (
            <Navbar bg="light" expand="lg">
                <Navbar.Brand href="#">qb4j</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="mr-auto">
                        <NavDropdown title="Databases" id="basic-nav-dropdown">
                            {availableDatabases}
                        </NavDropdown>

                        <Nav.Link className={this.props.menuBar.schemasAndTables.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleSubQueriesVisibility}
                        >
                            Sub Queries
                        </Nav.Link>

                        <Nav.Link className={this.props.menuBar.schemasAndTables.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleSchemasAndTablesVisibility}
                        >
                            Schemas & Tables
                        </Nav.Link>

                        <Nav.Link className={this.props.menuBar.joins.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleJoinsVisibility}
                        >
                            Joins
                        </Nav.Link>

                        <Nav.Link className={this.props.menuBar.columns.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleColumnsVisibility}
                        >
                            Columns
                        </Nav.Link>

                        <Nav.Link className={this.props.menuBar.criteria.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleCriteriaVisibility}
                        >
                            Criteria
                        </Nav.Link>

                        <Nav.Link className={this.props.menuBar.otherOptions.isVisible ? "nav-item active" : "nav-item"}
                                  onClick={this.props.toggleOtherOptionsVisibility}
                        >
                            Other Options
                        </Nav.Link>
                    </Nav>

                    <Button className="mr-1"
                            variant="outline-primary"
                            onClick={this.onRunQueryHandler}
                    >
                        Run Query
                    </Button>

                    <Button variant="outline-secondary"
                            onClick={this.props.onSaveQueryHandler}
                    >
                        Save Query
                    </Button>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    return reduxState
};

const mapDispatchToProps = (dispatch) => {
    return {
        onSaveQueryHandler: () => {
            dispatch({
                type: 'SHOW_SAVE_QUERY_MODAL',
                payload: {
                    hide: false
                }
            });
        },
        toggleSubQueriesVisibility: () => dispatch({ type: Constants.SUB_QUERIES }),
        toggleJoinsVisibility: () => dispatch({ type: Constants.JOINS }),
        toggleSchemasAndTablesVisibility: () => dispatch({ type: Constants.SCHEMAS_AND_TABLES }),
        toggleQueryTemplatesVisibility: () => dispatch({ type: Constants.QUERY_TEMPLATES }),
        toggleColumnsVisibility: () => dispatch({ type: Constants.COLUMNS }),
        toggleCriteriaVisibility: () => dispatch({ type: Constants.CRITERIA }),
        toggleOtherOptionsVisibility: () => dispatch({ type: Constants.OTHER_OPTIONS }),
        updateAvailableDatabases: (availableDatabases) => {
            dispatch({
                type: 'UPDATE_AVAILABLE_DATABASES',
                payload: {
                    availableDatabases: availableDatabases
                }
            })
        },
        onChangeSelectedDatabase: (selectedDatabase) => {
            // Update selected database.
            dispatch({
                type: 'CHANGE_SELECTED_DATABASE',
                payload: {
                    selectedDatabase: selectedDatabase
                }
            });

            // Get available schemas for the database.
            let apiUrl = `${window.location.origin}/metadata/${selectedDatabase.databaseName}/schema`;
            fetch(apiUrl)
                .then(response => response.json())
                .then(schemas => {
                    console.log(schemas);

                    // Update the schemas in the redux state.
                    dispatch({
                        type: 'UPDATE_AVAILABLE_SCHEMAS',
                        payload: {
                            availableSchemas: schemas
                        }
                    })
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

export default connect(mapReduxStateToProps, mapDispatchToProps)(MenuBar);
