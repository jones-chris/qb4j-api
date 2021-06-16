import React from 'react';
import './App.css';
import MenuBar from "./MenuBar/MenuBar";
import Joins from "./Joins/Joins";
import { connect } from 'react-redux'
import SchemasAndTables from "./SchemasAndTables/SchemasAndTables";
import Columns from "./Columns/Columns";
import OtherOptions from "./OtherOptions/OtherOptions";
import Criteria from "./Criteria/Criteria";
import ColumnValues from "./Modals/ColumnValues/ColumnValues";
import Warnings from "./Warnings/Warnings";
import {assertAllValidations} from "./Validators/Validators";
import SaveQuery from "./Modals/SaveQuery/SaveQuery";
import SubQueries from "./SubQueries/SubQueries";


class App extends React.Component {

    constructor(props) {
        super(props);

        // Get config state.
        this.props.onLoadIFrame();
    }

    componentDidMount() {
    }

    render() {
        return (
            <div className="App">
                <MenuBar/>

                <Warnings/>

                {/*Are the non-hidden attributes needed now that the state is available in the component?*/}
                <Joins
                    hidden={this.props.menuBar.joins.isHidden.toString()}
                />

                {/*Are the non-hidden attributes needed now that the state is available in the component?*/}
                <SchemasAndTables
                    hidden={this.props.menuBar.schemasAndTables.isHidden.toString()}
                />

                {/*Are the non-hidden attributes needed now that the state is available in the component?*/}
                <Columns
                    hidden={this.props.menuBar.columns.isHidden.toString()}
                />

                <OtherOptions
                    hidden={this.props.menuBar.otherOptions.isHidden.toString()}
                />

                <Criteria
                    hidden={this.props.menuBar.criteria.isHidden.toString()}
                />

                <SubQueries
                    hidden={this.props.menuBar.subQueries.isHidden.toString()}
                />

                {/*Modals*/}
                <ColumnValues
                    hidden={this.props.modal.hideColumnMembersModal.toString()}
                    modalState={this.props.modal.columnValueModal}
                />

                <SaveQuery
                    show={(this.props.modal.hideSaveQueryModal) ? 'false' : 'true'}
                />

            </div>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    return reduxState;
};

const mapDispatchToProps = (dispatch) => {
    return {
        onLoadIFrame: () => {
            dispatch({
                type: 'ADD_BASE_API_URL',
                payload: {
                    parentWindow: window.parent,
                    parentWindowUrl: document.location.ancestorOrigins[0]
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

export default connect(mapReduxStateToProps, mapDispatchToProps)(App);
