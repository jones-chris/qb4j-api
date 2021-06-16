import React from 'react';
import ReactDOM from 'react-dom';
import {combineReducers, createStore} from "redux";
import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import menuBarReducer from "./Store/MenuBarReducer";
import joinReducer from './Store/JoinReducer';
import queryReducer from "./Store/QueryReducer";
import { Provider } from 'react-redux'
import configReducer from "./Store/ConfigReducer";
import modalReducer from './Store/ModalReducer';

export const store = createStore(combineReducers(
    {
        config: configReducer,
        menuBar: menuBarReducer,
        joins: joinReducer,
        query: queryReducer,
        modal: modalReducer
    }
));

ReactDOM.render(
    <Provider store={store}>
        <App />
    </Provider>
    , document.getElementById('root'));
registerServiceWorker();
