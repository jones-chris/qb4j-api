import {store} from "../index";
import { replaceParentCriterionIds } from "../actions/CriteriaActions";
import { removeJoinMetadata } from "../actions/JoinActions";
import * as Utils from "../Utils/Utils"; 

export const runQuery = () => {
	let statement = buildSelectStatement();
    console.log(statement);
    console.log(JSON.stringify(statement));

    // Send query to API.
    let apiUrl = `${window.location.origin}/data/${store.getState().query.selectedDatabase.databaseName}/query`;
    fetch(apiUrl, {
        method: 'POST',
        body: JSON.stringify(statement),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(async response => {
        const data = await response.json();

        if (! response.ok) {
            if (data.hasOwnProperty('message')) {
                throw Error(data.message)
            } else {
                throw Error('An error occurred when running the query')
            }
        }

        return data;
    }).then(json => {
        console.log(json);

        // Send json to window's parent so the parent can choose what to do with the data.
        let parentWindow = store.getState().config.parentWindow;
        let parentWindowUrl = store.getState().config.parentWindowUrl;
        parentWindow.postMessage(json, parentWindowUrl);
    }).catch(reason => {
        alert(reason)
    });
};

export const saveQuery = () => {
    let statement = buildSelectStatement();
    console.log(statement);
    console.log(JSON.stringify(statement));

    // Send query to API.
    let apiUrl = `${window.location.origin}/query-template`;
    fetch(apiUrl, {
        method: 'POST',
        body: JSON.stringify(statement),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(async response => {
        if (response.ok) {
            alert('Query saved successfully!');
        } else {
            const data = await response.json();
            if (data.hasOwnProperty('message')) {
                throw Error(data.message)
            } else {
                throw Error('An error occurred when running the query')
            }
        }
    }).catch(reason => {
        alert(reason)
    });
};

const buildSelectStatement = () => {
    const currentQueryState = store.getState().query;
    const currentJoinState = store.getState().joins;

    // Determine parent table.
    let targetJoinTables = currentQueryState.joins.map(join => join.targetTable.fullyQualifiedName);
    let parentTable = currentQueryState.selectedTables.find(table => ! targetJoinTables.includes(table.fullyQualifiedName));

    // Build the common table expressions/sub queries.
    let commonTableExpressions = [];
    currentQueryState.subQueries.forEach(subQuery => {
        commonTableExpressions.push({
            name: subQuery.subQueryName,
            queryName: subQuery.queryTemplateName,
            parametersAndArguments: subQuery.parametersAndArguments,
            version: subQuery.version
        })
    });

    // Build statement object
    let statement = {
        metadata: currentQueryState.metadata,
        database: currentQueryState.selectedDatabase,
        columns: currentQueryState.selectedColumns,
        table: parentTable,
        criteria: replaceParentCriterionIds(currentQueryState.criteria),
        joins: removeJoinMetadata(currentJoinState.joins),
        distinct: currentQueryState.distinct,
        groupBy: false,
        orderBy: false,
        limit: currentQueryState.limit,
        ascending: currentQueryState.ascending,
        offset: currentQueryState.offset,
        suppressNulls: currentQueryState.suppressNulls,
        commonTableExpressions: commonTableExpressions
    };

    return statement;
};
