let _BACKGROUND_CONTAINER = null;
let _BACKGROUND_IMAGE_DIV = null;

let _initialBackgroundImageWidth = 0;
let _initialBackgroundImageHeight = 0;

let _visible = false;
let _created = false;

export function isCreated() {
    return _created;
}

export function show() {
    if (!_created) {
        console.error('failed to show background - it is not created');
        return;
    }
    if (_visible) {
        return;
    }
    resize();
    window.addEventListener('resize', resize);
    _visible = true;
}

export function hide() {
    if (!_created || !_visible) {
        return;
    }
    getBackgroundImageDiv().style.display = 'none';
    window.removeEventListener('resize', resize);
    _visible = false;
}

export function isVisible() {
    return _visible;
}

function resize() {
    let backgroundImageDiv = getBackgroundImageDiv();
    let windowWidth = window.innerWidth;
    let windowHeight = window.innerHeight;
    let widthProportion = windowWidth / _initialBackgroundImageWidth;
    let heightProportion = windowHeight / _initialBackgroundImageHeight;
    backgroundImageDiv.style.setProperty('background-size',
        heightProportion > widthProportion ? 'auto 100vh' : '100vw',
        'important');
    backgroundImageDiv.style.setProperty('display', 'block', 'important');
}

export function renderBackground(parentElement) {
    if (_created) {
        return;
    }
    const backGroundContainer = getBackgroundContainer();
    parentElement.appendChild(backGroundContainer);
    _created = true;
    _visible = true;
    const backgroundImg = backGroundContainer.firstElementChild;
    backgroundImg.onload = () => {
        _initialBackgroundImageWidth = backgroundImg.naturalWidth;
        _initialBackgroundImageHeight = backgroundImg.naturalHeight;
        backgroundImg.remove();
        resize();
        window.addEventListener('resize', resize);
    }
}

function getBackgroundContainer() {
    if (_BACKGROUND_CONTAINER === null) {
        _BACKGROUND_CONTAINER = document.createElement('div');
        _BACKGROUND_CONTAINER.innerHTML = getBackgroundHTMLContent();
    }
    return _BACKGROUND_CONTAINER;
}

function getBackgroundImageDiv() {
    if (_BACKGROUND_IMAGE_DIV === null) {
        _BACKGROUND_IMAGE_DIV = getBackgroundContainer().lastElementChild;
    }
    return _BACKGROUND_IMAGE_DIV;
}

function getBackgroundHTMLContent() {
    return `
<img id="backgroundImg" src="/images/start-background.jpg">
<div id="backgroundImageDiv" class="full-size-background"></div>
    `;
}
