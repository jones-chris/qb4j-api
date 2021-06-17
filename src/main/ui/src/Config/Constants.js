import leftJoinExcluding from "../Images/left_join_excluding.png";
import leftJoin from "../Images/left_join.png";
import innerJoin from "../Images/inner_join.png";
import rightJoin from "../Images/right_join.png";
import rightJoinExcluding from "../Images/right_join_excluding.png";
import fullOuterJoin from "../Images/full_outer_join.png";
import fullOuterJoinExcluding from "../Images/full_outer_join_excluding.png";


// HTML section names.
export const SUB_QUERIES = 'Sub Queries';
export const JOINS = 'Joins';
export const SCHEMAS_AND_TABLES = 'Schemas & Tables';
export const COLUMNS = 'Columns';
export const CRITERIA = 'Criteria';
export const OTHER_OPTIONS = 'Other Options';
export const QUERY_TEMPLATES = 'Query Templates';

// Join images.
export const JOIN_IMAGES = [
    {'name': 'LEFT_EXCLUDING',         'image': leftJoinExcluding},
    {'name': 'LEFT',                   'image': leftJoin},
    {'name': 'INNER',                  'image': innerJoin},
    {'name': 'RIGHT',                  'image': rightJoin},
    {'name': 'RIGHT_EXCLUDING',        'image': rightJoinExcluding},
    {'name': 'FULL_OUTER',             'image': fullOuterJoin},
    {'name': 'FULL_OUTER_EXCLUDING',   'image': fullOuterJoinExcluding}
];

// Criterion model object attributes that can be updated.
export const CONJUNCTION = 'conjunction';
export const COLUMN = 'column';
export const OPERATOR = 'operator';
export const FILTER = 'filter';
