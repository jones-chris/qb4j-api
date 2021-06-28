(function() {
    // Create the connector object
    var myConnector = tableau.makeConnector();

    // Define the schema
    myConnector.getSchema = function(schemaCallback) {
        let queryResults = JSON.parse(localStorage.getItem('qb4j-results'));

        let cols = [];
        queryResults.selectStatement
            .columns
            .forEach(column => cols.push(
                {
                    id: column.alias,
                    dataType: getTableauDataType(column.dataType)
                }
            ));

        var tableSchema = {
            id: "qb4j",
            alias: "The result of your query you built in qb4j",
            columns: cols
        };

        schemaCallback([tableSchema]);
    };

    // Download the data
    myConnector.getData = function(table, doneCallback) {
        console.log('Inside getData callback');

        let queryResults = JSON.parse(localStorage.getItem('qb4j-results'));

        // This will hold all the table data rows.
        let tableData = [];

        // Loop through each of the query result's data rows.
        for (let i=0; i<queryResults.data.length; i++) {

            // Build a table data row object for each query result's data row.
            let tableDataRow = {};
            for (let j=0; j<queryResults.selectStatement.columns.length; j++) {

                // Get the column alias.
                let columnAlias = queryResults.selectStatement.columns[j].alias;

                // Add the key-value pair (column alias is the key and data is the value) to the table data row object.
                tableDataRow[columnAlias] = queryResults.data[i][j];
            }

            // After all columns are iterated through, then add the object to the table data array.
            tableData.push(tableDataRow);
        }

        // Add the table data array to the tableau table.
        table.appendRows(tableData);


        console.log('Calling doneCallback');
        doneCallback();

        console.log('Exiting getData');
    };

    tableau.registerConnector(myConnector);


    // Create event listeners for when the iframe posts a message to the parent window.
    window.onmessage = (event) => {
        console.log('In onmessage handler');

        // if (event.origin === 'http://dev.api.querybuilder4j.net/') {
            console.log(event);

            localStorage.setItem('qb4j-results', JSON.stringify(event.data));

            tableau.connectionName = "qb4j"; // This will be the data source name in Tableau
            tableau.submit(); // This sends the connector object to Tableau
        // }
    }

})();

function getTableauDataType(jdbcSqlTypeInt) {
    /*
    Tableau data types as of 6/22/2021:
    bool: "bool"
    date: "date"
    datetime: "datetime"
    float: "float"
    geometry: "geometry"
    int: "int"
    string: "string"
    */

    if (jdbcSqlTypeInt === 2003)      { return tableau.dataTypeEnum.string }
    else if (jdbcSqlTypeInt === -5)   { return tableau.dataTypeEnum.int }
    // else if (jdbcSqlTypeInt === -2)   { return BINARY }
    else if (jdbcSqlTypeInt === -7)   { return tableau.dataTypeEnum.bool }
    // else if (jdbcSqlTypeInt === 2004) { return BLOB }
    else if (jdbcSqlTypeInt === 16)   { return tableau.dataTypeEnum.bool }
    else if (jdbcSqlTypeInt === 1)    { return tableau.dataTypeEnum.string }
    // else if (jdbcSqlTypeInt === 2005) { return CLOB }
    // else if (jdbcSqlTypeInt === 70)   { return DATA_LINK }
    else if (jdbcSqlTypeInt === 91)   { return tableau.dataTypeEnum.date }
    else if (jdbcSqlTypeInt === 3)    { return tableau.dataTypeEnum.float }
    // else if (jdbcSqlTypeInt === 2001) { return DISTINCT }
    else if (jdbcSqlTypeInt === 8)    { return tableau.dataTypeEnum.float }
    else if (jdbcSqlTypeInt === 6)    { return tableau.dataTypeEnum.float }
    else if (jdbcSqlTypeInt === 4)    { return tableau.dataTypeEnum.int }
    else if (jdbcSqlTypeInt === -16)  { return tableau.dataTypeEnum.string }
    else if (jdbcSqlTypeInt === -1)   { return tableau.dataTypeEnum.string }
    else if (jdbcSqlTypeInt === -15)  { return tableau.dataTypeEnum.string }
    else if (jdbcSqlTypeInt === 2)    { return tableau.dataTypeEnum.float }
    else if (jdbcSqlTypeInt === -9)   { return tableau.dataTypeEnum.string }
    else if (jdbcSqlTypeInt === 5)    { return tableau.dataTypeEnum.int }
    else if (jdbcSqlTypeInt === 92)   { return tableau.dataTypeEnum.datetime }
    else if (jdbcSqlTypeInt === 2013) { return tableau.dataTypeEnum.datetime }
    else if (jdbcSqlTypeInt === 93)   { return tableau.dataTypeEnum.datetime }
    else if (jdbcSqlTypeInt === 2014) { return tableau.dataTypeEnum.datetime }
    else if (jdbcSqlTypeInt === -6)   { return tableau.dataTypeEnum.int }
    else if (jdbcSqlTypeInt === 12)   { return tableau.dataTypeEnum.string }
    else { console.log('Did not recognize sql type') }
}