import React, { Component } from 'react';
import './Joins.css'
import {connect} from "react-redux";
import {
    addJoin,
    deleteJoin,
    changeJoinType,
    changeTable,
    addJoinColumn,
    changeColumn,
    deleteJoinColumn
} from "../actions/JoinActions";
import {assertAllValidations} from "../Validators/Validators";


class Joins extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        // Create joins JSX.
        const joinsJsx = this.props.joins.map(join => {
            // Create available tables JSX for parent table select element.
            const availableTablesParentTableOptions = this.props.availableTables.map(table => {
                return <option key={table.fullyQualifiedName}
                               value={table.fullyQualifiedName}
                               selected={join.parentTable.fullyQualifiedName === table.fullyQualifiedName}
                >
                    {table.tableName}
                </option>
            });

            // Create available tables JSX for target table select element.
            const availableTablesTargetTableOptions = this.props.availableTables.map(table => {
                return <option key={table.fullyQualifiedName}
                               value={table.fullyQualifiedName}
                               selected={join.targetTable.fullyQualifiedName === table.fullyQualifiedName}
                >
                    {table.tableName}
                </option>
            });

            // Create a join column div that includes a list of the parent join columns and target join columns.
            let joinColumns = [];
            if (join.parentJoinColumns !== undefined) {
                join.parentJoinColumns.forEach((parentColumn, index) => {
                    // Create available parent and target columns JSX.

                    // Loop through each item in available columns and target columns and set `selected` proprety to True
                    // for the parent join column and target join column.
                    let availableParentColumns = [];
                    let availableTargetColumns = [];
                    join.metadata.availableColumns.parentColumns.forEach((availableParentColumn, index) => {
                        availableParentColumns.push(
                            <option key={availableParentColumn.fullyQualifiedName}
                                    data-index={index}
                                    value={availableParentColumn.fullyQualifiedName}
                                    selected={availableParentColumn.fullyQualifiedName === parentColumn.fullyQualifiedName}
                            >
                                {availableParentColumn.columnName}
                            </option>
                        );
                    });

                    join.metadata.availableColumns.targetColumns.forEach(availableTargetColumn => {
                        availableTargetColumns.push(
                            <option key={availableTargetColumn.fullyQualifiedName}
                                    value={availableTargetColumn.fullyQualifiedName}
                                    selected={availableTargetColumn.fullyQualifiedName === join.targetJoinColumns[index].fullyQualifiedName}
                            >
                                {availableTargetColumn.columnName}
                            </option>
                        );
                    });

                    let parentJoinColumnsId = `joins${join.metadata.id}.parentJoinColumns${index}`;
                    let targetJoinColumnsId = `joins${join.metadata.id}.targetJoinColumns${index}`;

                    joinColumns.push(
                        <div key={index} data-index={index}>
                            <select id={parentJoinColumnsId}
                                    data-index={index}
                                    onChange={() => this.props.changeColumn(join.metadata.id, parentJoinColumnsId, targetJoinColumnsId)}
                            >
                                {availableParentColumns}
                            </select>

                            <b> = </b>

                            <select id={targetJoinColumnsId}
                                    data-index={index}
                                    onChange={() => this.props.changeColumn(join.metadata.id, parentJoinColumnsId, targetJoinColumnsId)}
                            >
                                {availableTargetColumns}
                            </select>

                            <span>
                                {/*Add parent and target column (the ON clause of the JOIN clause)*/}
                                <button id={`joins${join.metadata.id}.addParentAndTargetColumn`} type="button"
                                        onClick={() => this.props.addJoinColumn(join.metadata.id)}
                                >
                                    +
                                </button>

                                {/*Delete parent and target column (the ON clause of the JOIN clause*/}
                                <button id={`joins${join.metadata.id}.addParentAndTargetColumn`} type="button"
                                        onClick={() => this.props.deleteJoinColumn(join.metadata.id, index)}
                                >
                                    X
                                </button>
                            </span>

                        </div>
                    )
                });
            }

            return <div key={join.metadata.id} id={`join-row${join.metadata.id}`} className="join-row">
                {/*Delete join*/}
                <button id={`joins-deleteButton-${join.metadata.id}`} className="delete-join-button" type="button"
                        onClick={() => this.props.deleteJoin(join.metadata.id)}>
                    X
                </button>

                <input id={`joins${join.metadata.id}.joinType`} hidden defaultValue={join.joinType}/>

                <select id={`joins${join.metadata.id}.parentTable`}
                        onChange={(event) => this.props.changeTable(join.metadata.id, `joins${join.metadata.id}.parentTable`, `joins${join.metadata.id}.targetTable`)}
                >
                    {availableTablesParentTableOptions}
                </select>

                <img id={`joins-image-${join.metadata.id}`}
                     className="join-image"
                     src={join.metadata.joinImageUrl}
                     onClick={() => this.props.changeJoinType(join.metadata.id)}/>

                <select id={`joins${join.metadata.id}.targetTable`}
                        onChange={(event) => this.props.changeTable(join.metadata.id, `joins${join.metadata.id}.parentTable`, `joins${join.metadata.id}.targetTable`)}
                >
                    {availableTablesTargetTableOptions}
                </select>

                {/*Join columns*/}
                {joinColumns}
            </div>
        });

        return (
            <div id="joinsDiv" className="joins-div" hidden={this.props.hidden === 'true'}>
                <button id="addJoin" name="addJoin" type="button"
                        onClick={this.props.addJoin}
                >
                    Add Join
                </button>

                <div>
                    {joinsJsx}
                </div>
            </div>
        );
    }
}

const mapReduxStateToProps = (reduxState) => {
    let props = reduxState.joins;
    props['availableTables'] = reduxState.query.selectedTables;
    return props;
};

const mapDispatchToProps = (dispatch) => {
    return {
        addJoin: () => {
            dispatch(addJoin());

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        deleteJoin: (joinId) => {
            dispatch(deleteJoin(joinId));

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
        },
        changeJoinType: (joinId) => dispatch(changeJoinType(joinId)),
        changeTable: (joinId, parentTableElementName, targetTableElementName) => {
            dispatch(
                changeTable(joinId, parentTableElementName, targetTableElementName)
            );
        },
        changeColumn: (joinId, parentJoinColumnsElementId, targetJoinColumnsElementId) => {
            dispatch(
                changeColumn(joinId, parentJoinColumnsElementId, targetJoinColumnsElementId)
            );
        },
        addJoinColumn: (joinId) => dispatch(addJoinColumn(joinId)),
        deleteJoinColumn: (joinId, joinColumnIndex) => {
            dispatch(
                deleteJoinColumn(joinId, joinColumnIndex)
            );
        }
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(Joins);
