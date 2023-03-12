let _START_PAGE = null;
let _BACKGROUND_DIV = null;
let _SUBMIT_PLAYER_NAME_BUTTON = null;
let _PLAYER_NAME_INPUT = null;

let initialBackgroundImageWidth = 0;
let initialBackgroundImageHeight = 0;

export function getPlayerNameFromInput() {
    const playerNameInput = getPlayerNameInput();
    const name = playerNameInput.value;
    playerNameInput.value = '';
    return name;
}

export function getStartPageElement() {
    if (_START_PAGE === null) {
        _START_PAGE = document.createElement('div');
        _START_PAGE.className = "start-page-container";
        _START_PAGE.innerHTML = getStartPageHTMLContent();
    }
    return _START_PAGE;
}

export function resizeBackgroundImage() {
    let backgroundImageDiv = getBackgroundImageDiv();
    let windowWidth = window.innerWidth;
    let windowHeight = window.innerHeight;
    let widthProportion = windowWidth / initialBackgroundImageWidth;
    let heightProportion = windowHeight / initialBackgroundImageHeight;
    backgroundImageDiv.style.backgroundSize = heightProportion > widthProportion ? `auto 100vh` : `100vw`;
    backgroundImageDiv.style.display = 'block';
}

export function getSubmitPlayerNameButton() {
    if (_SUBMIT_PLAYER_NAME_BUTTON === null) {
        _SUBMIT_PLAYER_NAME_BUTTON = document.getElementById('submitPlayerName');
    }
    return _SUBMIT_PLAYER_NAME_BUTTON;
}

export function getPlayerNameInput() {
    if (_PLAYER_NAME_INPUT === null) {
        _PLAYER_NAME_INPUT = document.getElementById('playerNameInput');
    }
    return _PLAYER_NAME_INPUT;
}

export function initialiseBackground(backgroundImg) {
    if (initialBackgroundImageHeight === 0 && initialBackgroundImageWidth === 0) {
        initialBackgroundImageWidth = backgroundImg.naturalWidth;
        initialBackgroundImageHeight = backgroundImg.naturalHeight;
        backgroundImg.remove();
    }
}

function getBackgroundImageDiv() {
    if (_BACKGROUND_DIV === null) {
        _BACKGROUND_DIV = document.getElementById('backgroundImageDiv');
    }
    return _BACKGROUND_DIV;
}

function getStartPageHTMLContent() {
    return `
<img id="backgroundImg" src="/images/start-background.jpg" style="display: none">
<div id="backgroundImageDiv" class="full-size-background"></div>
<div id="welcome" class="welcome-message">Welcome to the Monopoly game!</div>
<label for="playerNameInput" class="enter-nickname-phrase">Enter your nickname:</label>
<input id="playerNameInput" class="player-name-input" type="text" autocomplete="off" autofocus/>
<button id="submitPlayerName" class="start-page-button, join-game-button">Join the game</button>
    `;
}
