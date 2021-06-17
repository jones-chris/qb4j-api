export const ARRAY = 'array';
export const BIG_INT = 'big int';
export const BINARY = 'binary';
export const BIT = 'bit';
export const BLOB = 'blob';
export const BOOLEAN = 'boolean';
export const CHAR = 'char';
export const CLOB = 'clob';
export const DATA_LINK = 'data link';
export const DATE = 'date';
export const DECIMAL = 'decimal';
export const DISTINCT = 'distinct';
export const DOUBLE = 'double';
export const FLOAT = 'float';
export const INTEGER = 'integer';
export const LONGNVARCHAR = 'longnvarchar';
export const LONGVARCHAR = 'longvarchar';
export const NCHAR = 'nchar';
export const NUMERIC = 'numeric';
export const NVARCHAR = 'nvarchar';
export const SMALL_INT = 'small int';
export const TIME = 'time';
export const TIME_WITH_TIMEZONE = 'time with timezone';
export const TIMESTAMP = 'timestamp';
export const TIMESTAMP_WITH_TIMEZONE = 'timestamp with timezone';
export const TINY_INT = 'tiny int';
export const VARCHAR = 'varchar';

export function getJdbcSqlType(jdbcSqlTypeInt) {
    if (jdbcSqlTypeInt === 2003) { return ARRAY }
    else if (jdbcSqlTypeInt === -5) { return BIG_INT }
    else if (jdbcSqlTypeInt === -2) { return BINARY }
    else if (jdbcSqlTypeInt === -7) { return BIT }
    else if (jdbcSqlTypeInt === 2004) { return BLOB }
    else if (jdbcSqlTypeInt === 16) { return BOOLEAN }
    else if (jdbcSqlTypeInt === 1) { return CHAR }
    else if (jdbcSqlTypeInt === 2005) { return CLOB }
    else if (jdbcSqlTypeInt === 70) { return DATA_LINK }
    else if (jdbcSqlTypeInt === 91) { return DATE }
    else if (jdbcSqlTypeInt === 3) { return DECIMAL }
    else if (jdbcSqlTypeInt === 2001) { return DISTINCT }
    else if (jdbcSqlTypeInt === 8) { return DOUBLE }
    else if (jdbcSqlTypeInt === 6) { return FLOAT }
    else if (jdbcSqlTypeInt === 4) { return INTEGER }
    else if (jdbcSqlTypeInt === -16) { return LONGNVARCHAR }
    else if (jdbcSqlTypeInt === -1) { return LONGVARCHAR }
    else if (jdbcSqlTypeInt === -15) { return NCHAR }
    else if (jdbcSqlTypeInt === 2) { return NUMERIC }
    else if (jdbcSqlTypeInt === -9) { return NVARCHAR }
    else if (jdbcSqlTypeInt === 5) { return SMALL_INT }
    else if (jdbcSqlTypeInt === 92) { return TIME }
    else if (jdbcSqlTypeInt === 2013) { return TIME_WITH_TIMEZONE }
    else if (jdbcSqlTypeInt === 93) { return TIMESTAMP }
    else if (jdbcSqlTypeInt === 2014) { return TIMESTAMP_WITH_TIMEZONE }
    else if (jdbcSqlTypeInt === -6) { return TINY_INT }
    else if (jdbcSqlTypeInt === 12) { return VARCHAR }
    else { console.log('Did not recognize sql type') }
}

export function getSelectedOptions(selectElement) {
    const options = selectElement.options;

    let selectedOptions = [];
    for (let i=0; i<options.length; i++) {
        let option = options[i];
        if (option.selected) {
            selectedOptions.push(option.value);
        }
    }

    return selectedOptions;
}

export function getCriterionFilterParameters(criterion) {
    return criterion.filter.values.filter(value => value.startsWith('@'));
}

export function getCriterionFilterSubQueries(criterion) {
    return criterion.filter.values.filter(value => value.startsWith('$'));
}

export function getCriterionFilterValues(criterion) {
    return criterion.filter.values.filter(value => ! value.startsWith('$') && ! value.startsWith('@'));
}
