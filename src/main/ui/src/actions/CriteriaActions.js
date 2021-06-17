import {store} from "../index";
import * as Constants from '../Config/Constants';

export const addCriterion = (parentCriterion) => {
    // Copy the state's criteria to a new array.
    let newCriteria = [...store.getState().query.criteria];

    // Get the new criterion's level.  This will be the parent criterion's level plus 1.
    let level = 0;
    if (parentCriterion !== null) {
        level = parentCriterion.metadata.level + 1;
    }

    // Determine the criterion's column.
    let currentQueryState = store.getState().query;
    let column = null;
    if (currentQueryState.availableColumns.length > 0) {
        column = currentQueryState.availableColumns[0];
    }

    // Instantiate a new criterion model with the id and parent id.
    let criterion = {
        parentCriterion: parentCriterion,
        conjunction: 'And',
        column: column,
        operator: 'equalTo',
        filter: {
            values: []
        },
        childCriteria: [],
        metadata: {
            level: level
        }
    };

    // Add the new criterion to the criteria array.  If the `parentCriterion` is null, then add the new criterion as a
    // root criterion.  Else, find the new criterion's parent criterion and add the new criterion to the parent's child
    // criteria.
    if (parentCriterion === null) {
        newCriteria.push(criterion);
    } else {
        let flattenedNewCriteria = flattenCriteria(newCriteria, []);
        flattenedNewCriteria.find(criterion => parentCriterion.metadata.id === criterion.metadata.id)
            .childCriteria
            .push(criterion);
    }

    setCriterionMetadata(newCriteria);

    return { type: 'ADD_CRITERIA', payload: { newCriteria: newCriteria } };
};

export const updateCriterion = (criterion, criterionObjectAttributeName, value) => {
    let newCriteria = [...store.getState().query.criteria];

    let flattenedNewCriteria = flattenCriteria(newCriteria, []);

    flattenedNewCriteria.forEach(thisCriterion => {
        if (thisCriterion.metadata.id === criterion.metadata.id) {

            // If the filter is being updated, then split the value into an array before updating the criterion's attribute.
            if (criterionObjectAttributeName === Constants.FILTER) {
                // An empty string means the values should be an empty array.  If the value is not an empty string, then
                // split it into an array, determine what items are sub queries, parameters, and values, and then assign
                // the array to the criterion's filter's values.
                if (value === '') {
                    thisCriterion[Constants.FILTER].values = [];
                } else {
                    const splitValues = value.split(',');
                    thisCriterion[Constants.FILTER].values = splitValues;
                }
            } else {
                // Otherwise, just assign the value to the attribute.
                thisCriterion[criterionObjectAttributeName] = value;
            }
        }
    });

    return { type: 'UPDATE_CRITERIA', payload: { newCriteria: newCriteria } };
};

export const deleteCriterion = (criterionToDelete) => {
    let newCriteria = [...store.getState().query.criteria];

    newCriteria = recursivelySearchAndDeleteCriterion(newCriteria, criterionToDelete);

    setCriterionMetadata(newCriteria);

    return { type: 'UPDATE_CRITERIA', payload: { newCriteria: newCriteria } };
};

const recursivelySearchAndDeleteCriterion = (criteria, criterionToDelete) => {
    let idToDelete = criterionToDelete.metadata.id;

    // criteria.forEach(criterion => {
    for (let index in criteria) {
        let criterion = criteria[index];
        if (criterion.metadata.id === idToDelete) {
            // Get criterion's parent.
            let parentCriterion = criterion.parentCriterion;

            // Set the criterion's childrens' parent criterion to the criterion's parent criterion, so that the children
            // don't reference the criterion once it's deleted.
            criterion.childCriteria.forEach(childCriterion => {
                childCriterion.parentCriterion = parentCriterion;
            });

            // If the child criteria will be promoted to roots, then add them to the `criteria` array.
            // Else, add the child criteria to the new parent criterion's children.
            criterionToDelete.childCriteria.forEach(childCriterion => {
                if (parentCriterion === null) {
                    criteria.push(childCriterion);
                } else {
                    parentCriterion.childCriteria.push(childCriterion);
                }
            });

            // Delete the criterion by using a filter to return only the criteria that do not match the `criterionId`.
            // If the `parentCriterion` is null, then filter the criteria, which should contain the root criteria.
            // Else, filter the `parentCriterion`'s children.
            if (parentCriterion === null) {
                criteria = criteria.filter(criterion => {
                    return criterion.metadata.id !== idToDelete;
                })
            } else {
                parentCriterion.childCriteria = parentCriterion.childCriteria.filter(childCriterion => {
                    return childCriterion.metadata.id !== idToDelete;
                });
            }

            // Now that we found the criterion to delete, break the FOR loop.
            break;
        }

        // If we did not find the criterion matching the `criterionId` yet, then continue the depth-first search.
        recursivelySearchAndDeleteCriterion(criterion.childCriteria, criterionToDelete);
    }

    return criteria;
};

/**
 * Sets each criterion's metadata (ex:  level, id, etc).
 *
 * @param criteria The array of criterion to set metadata for.
 * @param nextId The next integer id to be passed up and down the tree.
 * @returns The next id to be used in the next criterion in the tree (above this node).
 */
const setCriterionMetadata = (criteria, nextId = 0) => {
    criteria.forEach(criterion => {
        // Set id.
        criterion.metadata.id = nextId;
        nextId = nextId + 1;

        // Set level.
        if (criterion.parentCriterion === null) {
            criterion.metadata.level = 0;
        } else {
            criterion.metadata.level = criterion.parentCriterion.metadata.level + 1;
        }

        // Continue walking the tree's depth.
        nextId = setCriterionMetadata(criterion.childCriteria, nextId);
    });

    return nextId;
};

/**
 * Flattens the criteria array.
 *
 * @param criteria The array of criteria to flatten.
 * @param flattenedCriteriaHolder The array that holds the flattened criteria.
 * @returns An flattened array of criterion.
 */
export const flattenCriteria = (criteria, flattenedCriteriaHolder) => {
    criteria.forEach(criterion => {
        flattenedCriteriaHolder.push(criterion);
        flattenCriteria(criterion.childCriteria, flattenedCriteriaHolder);
    });

    return flattenedCriteriaHolder;
};

/**
 * Removes circular JSON references (the parentCriterion attribute) of each criterion so that each object can be
 * serialized to JSON to be sent to the API.
 *
 * @param criteria The array of criterion.
 * @returns A flattened array of the criterion with the circular JSON references replaced and attributes that the API
 * does not need removed.
 */
export const replaceParentCriterionIds = (criteria) => {
    let flattenedCriteria = flattenCriteria(criteria, []);

    return flattenedCriteria.map(criterion => {
        let criterionClone = Object.assign({}, criterion);

        if (criterionClone.parentCriterion !== null) {
            criterionClone.parentId = criterionClone.parentCriterion.metadata.id;
        } else {
            criterionClone.parentId = null;
        }

        criterionClone.id = criterionClone.metadata.id;

        delete criterionClone.parentCriterion;

        delete criterionClone.metadata;

        delete criterionClone.childCriteria;

        return criterionClone;
    });
};
