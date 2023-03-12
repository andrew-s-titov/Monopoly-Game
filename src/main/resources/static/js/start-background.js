let _BACKGROUND_CONTAINER = null;
let _BACKGROUND_IMAGE_DIV = null;

let _initialBackgroundImageWidth = 0;
let _initialBackgroundImageHeight = 0;

let _visible = false;
let _rendered = false;

export function isRendered() {
    return _rendered;
}

export function show() {
    if (!_rendered) {
        console.error('failed to show background - it is not rendered');
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
    if (!_rendered) {
        console.error('failed to hide background - it is not rendered');
        return;
    }
    if (!_visible) {
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
    backgroundImageDiv.style.backgroundSize = heightProportion > widthProportion ? `auto 100vh` : `100vw`;
    backgroundImageDiv.style.display = 'block';
}

export function renderBackground(parentElement) {
    if (_rendered) {
        return;
    }
    parentElement.appendChild(getBackgroundContainer());
    _rendered = true;
    const backgroundImg = document.getElementById('backgroundImg');
    backgroundImg.onload = () => {
        _initialBackgroundImageWidth = backgroundImg.naturalWidth;
        _initialBackgroundImageHeight = backgroundImg.naturalHeight;
        backgroundImg.remove();
        resize();
        window.addEventListener('resize', resize);
        _visible = true;
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
        _BACKGROUND_IMAGE_DIV = document.getElementById('backgroundImageDiv');
    }
    return _BACKGROUND_IMAGE_DIV;
}

function getBackgroundHTMLContent() {
    return `
<img id="backgroundImg" src="/images/start-background.jpg" style="display: none">
<div id="backgroundImageDiv" class="full-size-background"></div>
    `;
}
