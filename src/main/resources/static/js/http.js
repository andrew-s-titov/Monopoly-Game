export function sendGetHttpRequest(url, async, onRequesterLoadFunction, onRequesterErrorFunction) {
    sendHttpRequest('GET', url, async, onRequesterLoadFunction, onRequesterErrorFunction);
}

export function sendPostHttpRequest(url, async, onRequesterLoadFunction, onRequesterErrorFunction) {
    sendHttpRequest('POST', url, async, onRequesterLoadFunction, onRequesterErrorFunction);
}

function sendHttpRequest(method, url, async, onRequesterLoadFunction, onRequesterErrorFunction) {
    let requester = prepareHttpRequester(method, url, async);
    if (onRequesterLoadFunction != null) requester.onload = () => onRequesterLoadFunction(requester);
    if (onRequesterErrorFunction != null) requester.onerror = () => onRequesterErrorFunction(requester);
    requester.send();
}

function prepareHttpRequester(method, url, async) {
    let httpRequester = new XMLHttpRequest();
    httpRequester.open(method, url, async);
    return httpRequester;
}