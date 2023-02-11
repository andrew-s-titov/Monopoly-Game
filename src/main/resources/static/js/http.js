import {displayError} from './utils.js';

let _HOST = null;
let _SCHEME = null;
let _BASE_GAME_URL = null;
let _WEBSOCKET_SCHEME = null;
let _WEBSOCKET_BASE_URL = null;

export function setHost(host) {
    _HOST = host;
    const localhost = _HOST.includes('localhost', 0);
    _SCHEME = localhost ? 'http' : 'https';
    _WEBSOCKET_SCHEME = localhost ? 'ws' : 'wss';
    _BASE_GAME_URL = `${_SCHEME}://${_HOST}/game`;
    _WEBSOCKET_BASE_URL = `${_WEBSOCKET_SCHEME}://${_HOST}/connect`;
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

export function get(url, on2xx, on400) {
    fetch(url, fetchParams('GET'))
        .then(response => processResponse(response, on2xx, on400))
        .catch(error => processError(error));
}

export function post(url, body, on2xx, on400) {
    fetch(url, fetchParams('POST', body))
        .then(response => processResponse(response, on2xx, on400))
        .catch(error => processError(error));
}

function processResponse(response, on2xx, on400) {
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
    } else if (responseStatus === 400 && on400 !== undefined && on400 !== null) {
        const contentType = response.headers.get('Content-Type');
        let responseMessagePromise;
        if (contentType === 'application/json') {
            responseMessagePromise = response.json()
                .then(json => json.message);
        } else if (contentType.startsWith('text/plain')) {
            responseMessagePromise = response.text();
        } else {
            responseMessagePromise = Promise.resolve('Bad request');
        }
        responseMessagePromise.then(responseMessage => on400(responseMessage));

    } else if (responseStatus >= 500) {
        console.log('Unexpected server error');
        displayError('Unexpected server error. Please, reload the page');
    }
}

function processError(error) {
    console.log(error);
    displayError('Internet connection problem. Please, reload the page');
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