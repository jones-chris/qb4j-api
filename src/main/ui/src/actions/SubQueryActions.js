import {store} from "../index";

export async function updateSubQuery(subQueryId, attributeName, value) {
	let newSubQueries = [...store.getState().query.subQueries];

	newSubQueries.forEach(subQuery => {
		if (subQuery.id === subQueryId) {
			subQuery[attributeName] = value;
		}
	});

	return {
		type: 'UPDATE_SUBQUERIES',
		payload: {
			subQueries: newSubQueries
		}
	};
};

export async function updateVersions(subQueryId) {
	let newSubQueries = [...store.getState().query.subQueries];

	newSubQueries.forEach(subQuery => {
		// If the sub query has no versions, then get them from the API.  This is a blocking operation.
		if (subQuery.versions.length === 0) {
			let apiUrl = `${window.location.origin}/query-template/${subQuery.queryTemplateName}/versions`;
	        
	        fetch(apiUrl)
	            .then(response => response.json())
	            .then(versions => {
	                console.log(versions);
	                subQuery.versions = versions;
	            });
		}
	});

	return {
		type: 'UPDATE_SUBQUERIES',
		payload: {
			subQueries: newSubQueries
		}
	}

}