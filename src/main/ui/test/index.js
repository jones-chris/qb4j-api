window.onmessage = (event) => {
    console.log(event);

    let columns = event.data.columns;
    let data = event.data.data;
    let sql = event.data.sql;

    // Create SQL p element.
    let sqlElement = document.createElement('p');
    sqlElement.innerHTML = sql;

    // Create table.
    let table = document.createElement('table');
    table.id = 'queryResultsTable';

    // Create data rows.
    data.forEach((dataArray, dataArrayIndex) => {
        let dataRow = table.insertRow(dataArrayIndex);
        dataArray.forEach((dataItem, dataItemIndex) => {
            let cell = dataRow.insertCell(dataItemIndex);
            cell.innerHTML = dataItem;
        });
    });

    // Create header row last because rows are appended to the beginning of the table.
    let headerRow = table.insertRow(0);
    columns.forEach((header, index) => {
        // let cell = headerRow.insertCell(index);
        let cell = document.createElement('th');
        cell.innerHTML = header;
        headerRow.appendChild(cell);
    });

    // Delete exist query results table (if it exists), then add new query results table.
    let queryResultsDiv = document.getElementById('queryResults');

    if (queryResultsDiv.children.length > 0) {
        while (queryResultsDiv.lastElementChild) {
            queryResultsDiv.removeChild(queryResultsDiv.lastElementChild);
        }
    }

    queryResultsDiv.appendChild(sqlElement);
    queryResultsDiv.appendChild(table);
};
