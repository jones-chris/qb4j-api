import React from 'react';
import './OtherOptions.css'
import { connect } from "react-redux";

const OtherOptions = (props) => {
    return (
        <div id="otherOptionsDiv" className="other-options-div" hidden={props.hidden === 'true'}>
            <table className="other-options-table">
                <tbody>
                    <tr>
                        <td>Distinct / Unique Records Only</td>
                        <td>
                            <input id="distinct"
                                   type="checkbox"
                                   checked={props.distinct}
                                   onChange={props.onDistinctChangeHandler}
                            />
                        </td>
                    </tr>
                    <tr>
                        <td>Suppress Null Records</td>
                        <td>
                            <input id="suppressNulls"
                                   type="checkbox"
                                   checked={props.suppressNulls}
                                   onChange={props.onSuppressNullsChangeHandler}
                            />
                        </td>
                    </tr>
                    <tr>
                        <td>Limit Returned Records</td>
                        <td>
                            <select id="limit"
                                    value={props.limit}
                                    onChange={(event) => props.onLimitChangeHandler(event.target.value)}
                            >
                                <option value="">No Limit</option>
                                <option value="10">10</option>
                                <option value="50">50</option>
                                <option value="100">100</option>
                                <option value="500">500</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>Offset Returned Records</td>
                        <td>
                            <select id="offset"
                                    value={props.offset}
                                    onChange={(event) => props.onOffsetChangeHandler(event.target.value)}
                            >
                                <option value="">No Offset</option>
                                <option value="10">10</option>
                                <option value="50">50</option>
                                <option value="100">100</option>
                                <option value="500">500</option>
                            </select>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    );
};

const mapReduxStateToProps = (reduxState) => {
    return reduxState.query;
};

const mapDispatchToProps = (dispatch) => {
    return {
        onDistinctChangeHandler: () => dispatch({ type: 'UPDATE_DISTINCT' }),
        onSuppressNullsChangeHandler: () => dispatch({ type: 'UPDATE_SUPPRESS_NULLS' }),
        onLimitChangeHandler: (newLimit) => dispatch({ type: 'UPDATE_LIMIT', payload: { newLimit: newLimit } }),
        onOffsetChangeHandler: (newOffset) => dispatch({ type: 'UPDATE_OFFSET', payload: { newOffset: newOffset } })
    }
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(OtherOptions);
