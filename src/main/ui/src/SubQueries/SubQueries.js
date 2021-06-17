import React, { Component } from 'react';
import { connect } from 'react-redux'
import { store } from "../index";
import SubQuery from "./SubQuery/SubQuery";
import Button from "react-bootstrap/Button";
import {assertAllValidations} from "../Validators/Validators";
import "./SubQueries.css";
import Accordion from 'react-bootstrap/Accordion';

class SubQueries extends Component {

	constructor(props) {
		super(props);
	}

	render() {
		let subQueriesJsx = [];
		store.getState().query.subQueries.forEach(subQuery => {
			subQueriesJsx.push(
				<SubQuery key={subQuery.id}
						  subQuery={subQuery}
				>
				</SubQuery>
			);
		});

		return (
            <div hidden={this.props.hidden === 'true'} class="sub-queries">

            	<Button variant="outline-primary" onClick={this.props.onAddSubQuery}>
		            Add Sub Query
		        </Button>

		        <Accordion>
		        	{subQueriesJsx}
		        </Accordion>

            </div>
        );
	}

} 

const mapReduxStateToProps = (reduxState) => {
    return {
        ...reduxState.query
    }
};

const mapDispatchToProps = (dispatch) => {
    return {
    	onAddSubQuery: async () => {
    		let availableSubQueryNames = Object.keys(store.getState().query.availableSubQueries);
    		if (availableSubQueryNames.length === 0) {
    			let apiUrl = `${window.location.origin}/query-template`;
	            await fetch(apiUrl)
	                .then(response => response.json())
	                .then(queryTemplateNames => {
	                    console.log(queryTemplateNames);

	                    let subQueriesObject = {}
	                    queryTemplateNames.forEach(queryTemplateName => {
	                    	subQueriesObject[queryTemplateName] = {
	                    		versions: {} // Metadata should be the value of each version.
	                    	};
	                    })

	                    dispatch({
			    			type: 'UPDATE_AVAIALABLE_SUBQUERIES',
			    			payload: {
			    				availableSubQueries: subQueriesObject
			    			}
			    		});
	                });
    		}

    		let subQueries = [...store.getState().query.subQueries];
    		subQueries.push({
    			id: subQueries.length,
    			subQueryName: '',
    			queryTemplateName: '',
    			version: null,
    			parametersAndArguments: {}
    		});
    		dispatch({
    			type: 'UPDATE_SUBQUERIES',
    			payload: {
    				subQueries: subQueries
    			}
    		});

            dispatch({
                type: 'UPDATE_UI_MESSAGES',
                payload: {
                    uiMessages: assertAllValidations()
                }
            });
    	}
	}
};

export default connect(mapReduxStateToProps, mapDispatchToProps)(SubQueries);