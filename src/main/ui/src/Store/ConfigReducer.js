const initialState = {
    parentWindow: null,
    parentWindowUrl: ''
};

const configReducer = (state = initialState, action) => {

    switch (action.type) {
        case 'ADD_BASE_API_URL':
            return {
                ...state,
                parentWindow: action.payload.parentWindow,
                parentWindowUrl: action.payload.parentWindowUrl
            };
        default:
            return state;
    }

};

export default configReducer;
