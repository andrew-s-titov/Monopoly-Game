let HOST = null;
let SCHEME = null;
let BASE_GAME_URL = null;
let WEBSOCKET_SCHEME = null;
let WEBSOCKET_BASE_URL = null;

export function setHost(host) {
    HOST = host;
    const localhost = HOST.includes('localhost', 0);
    SCHEME = localhost ? 'http' : 'https';
    WEBSOCKET_SCHEME = localhost ? 'ws' : 'wss';
    BASE_GAME_URL = `${SCHEME}://${HOST}/game`;
    WEBSOCKET_BASE_URL = `${WEBSOCKET_SCHEME}://${HOST}/connect`;
}

export function getHost() {
    return HOST;
}

export function getBaseGameUrl() {
    return BASE_GAME_URL;
}

export function getBaseWebsocketUrl() {
    return WEBSOCKET_BASE_URL;
}

export function sendGetHttpRequest(url, async, onRequesterLoadFunction, onRequesterErrorFunction) {
    sendHttpRequest('GET', url, async, onRequesterLoadFunction, onRequesterErrorFunction);
}

export function sendPostHttpRequest(url, async, onRequesterLoadFunction, onRequesterErrorFunction, body) {
    sendHttpRequest('POST', url, async, onRequesterLoadFunction, onRequesterErrorFunction, body);
}

function sendHttpRequest(method, url, async, onRequesterLoadFunction, onRequesterErrorFunction, body) {
    const requester = prepareHttpRequester(method, url, async);
    if (onRequesterLoadFunction != null) requester.onload = () => onRequesterLoadFunction(requester);
    if (onRequesterErrorFunction != null) requester.onerror = () => onRequesterErrorFunction(requester);
    if (body) {
        requester.setRequestHeader("Content-Type", "application/json");
        requester.send(JSON.stringify(body));
    } else {
        requester.send();
    }
}

function prepareHttpRequester(method, url, async) {
    const httpRequester = new XMLHttpRequest();
    httpRequester.open(method, url, async);
    httpRequester.withCredentials = true;
    return httpRequester;
}