import React, { Component } from 'react';
import {connect} from "react-redux";


class Warnings extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        let uiMessages = [];
        for (let property in this.props) {
            if (this.props[property].hasOwnProperty('uiMessages')) {
                this.props[property].uiMessages.forEach(uiMessage => {
                    uiMessages.push(
                        <p className="warning">{uiMessage}</p>
                    )
                });
            }

        }

        return (
            <div>
                {uiMessages}
            </div>
        );
    }

}

const mapReduxStateToProps = (reduxState) => {
    return reduxState.menuBar
};

export default connect(mapReduxStateToProps, null)(Warnings);
