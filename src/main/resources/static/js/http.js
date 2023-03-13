import {displayError} from './utils.js';

let _HOST = null;
let _PROTOCOL = null;
let _BASE_GAME_URL = null;
let _WEBSOCKET_PROTOCOL = null;
let _WEBSOCKET_BASE_URL = null;

export function setConnectionData(protocol, host) {
    _PROTOCOL = protocol;
    _HOST = host;
    const localhost = _HOST.includes('localhost', 0);
    _WEBSOCKET_PROTOCOL = localhost ? 'ws:' : 'wss:';
    _BASE_GAME_URL = `${_PROTOCOL}//${_HOST}/game`;
    _WEBSOCKET_BASE_URL = `${_WEBSOCKET_PROTOCOL}//${_HOST}/connect`;
}

export function getHost() {
    return _HOST;
}

export function baseGameUrl() {
    return _BASE_GAME_URL;
}

export function getBaseWebsocketUrl() {
    return _WEBSOCKET_BASE_URL;
}

export function get(url, onSuccess) {
    fetch(url, fetchParams('GET'))
        .then(response => processResponse(response, onSuccess))
        .catch(error => processError(error));
}

export function post(url, body, onSuccess) {
    fetch(url, fetchParams('POST', body))
        .then(response => processResponse(response, onSuccess))
        .catch(error => processError(error));
}

function processResponse(response, on2xx) {
    const responseStatus = response.status;
    if (responseStatus >= 200 && responseStatus < 300 && on2xx !== undefined && on2xx !== null) {
        const contentType = response.headers.get('Content-Type');
        if (contentType === null) {
            on2xx();
        } else {
            if (contentType !== 'application/json') {
                console.warn(`unexpected response content type: ${contentType}`);
                return;
            }
            response.json()
                .then(responseBody => on2xx(responseBody));
        }
    } else if (responseStatus === 400) {
        const contentType = response.headers.get('Content-Type');
        if (contentType === 'application/json') {
            response.json()
                .then(json => {
                    const code = json.code;
                    const message = json.message;
                    if (code === 401) {
                        displayError(message);
                    } else if (code === 402) {
                        console.error(message);
                    }
                });
        } else {
            displayError('Bad request');
        }
    } else if (responseStatus >= 500) {
        console.log('Unexpected server error');
        displayError('Unexpected server error. Please, reload the page');
    }
}

function processError(error) {
    console.error(error);
    displayError('Internet connection problem. Please, try again');
}

function fetchParams(method, body) {
    if (body !== undefined && body !== null) {
        return {
            method: method,
            mode: 'cors',
            credentials: 'include',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        }
    } else {
        return {
            method: method,
            mode: 'cors',
            credentials: 'include'
        }
    }
}