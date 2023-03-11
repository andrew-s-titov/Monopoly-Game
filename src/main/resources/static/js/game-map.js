const _ATOP_MAP_CONTAINER_ID = 'atopMapContainer';
const _FULLSCREEN_SHADOW_ID = 'fullscreenShadow';

let _ATOP_MAP_CONTAINER = null;
let _FULLSCREEN_SHADOW = null;

export function displayAtopMapMessage(content) {
    if (content === undefined || content === null) {
        console.error('Cannot display empty content');
        return;
    }
    const messageContainer = getAtopMapContainer();
    if (content instanceof HTMLElement) {
        messageContainer.appendChild(content);
    } else {
        getAtopMapContainer().innerHTML = content;
    }
    getFullscreenShadowElement().style.setProperty('display', 'flex', 'important');
}

export function hideAtopMapMessage() {
    getAtopMapContainer().innerHTML = '';
    getFullscreenShadowElement().style.display = 'none';
}

function getAtopMapContainer() {
    if (_ATOP_MAP_CONTAINER === null) {
        _ATOP_MAP_CONTAINER = document.getElementById(_ATOP_MAP_CONTAINER_ID);
    }
    return _ATOP_MAP_CONTAINER;
}

function getFullscreenShadowElement() {
    if (_FULLSCREEN_SHADOW === null) {
        if (_ATOP_MAP_CONTAINER === null) {
            _FULLSCREEN_SHADOW = document.getElementById(_FULLSCREEN_SHADOW_ID);
        } else {
            _FULLSCREEN_SHADOW = _ATOP_MAP_CONTAINER.parentElement;
        }
    }
    return _FULLSCREEN_SHADOW;
}