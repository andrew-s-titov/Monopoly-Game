let _START_PAGE_CONTAINER = null;
let _SUBMIT_PLAYER_NAME_BUTTON = null;
let _PLAYER_NAME_INPUT = null;

let _rendered = false;

export function render(parentContainer) {
    if (_rendered) {
        return;
    }
    parentContainer.innerHTML = '';
    parentContainer.appendChild(getStartPageContainer());
    _rendered = true;
}

export function hide(parentContainer) {
    if (!_rendered) {
        return;
    }
    parentContainer.innerHTML = '';
    _rendered = false;
}

export function getPlayerNameFromInput() {
    if (!_rendered) {
        console.error('failed to get player name from input - start page is not rendered');
        return;
    }
    const playerNameInput = getPlayerNameInput();
    const name = playerNameInput.value;
    playerNameInput.value = '';
    return name;
}

function getStartPageContainer() {
    if (_START_PAGE_CONTAINER === null) {
        _START_PAGE_CONTAINER = document.createElement('div');
        _START_PAGE_CONTAINER.className = "start-page-container";
        _START_PAGE_CONTAINER.innerHTML = getStartPageHTMLContent();
    }
    return _START_PAGE_CONTAINER;
}

export function getSubmitPlayerNameButton() {
    if (!_rendered) {
        console.error('failed to get submitPlayerName button - start page is not rendered');
        return;
    }
    if (_SUBMIT_PLAYER_NAME_BUTTON === null) {
        _SUBMIT_PLAYER_NAME_BUTTON = getStartPageContainer().lastElementChild;
    }
    return _SUBMIT_PLAYER_NAME_BUTTON;
}

export function getPlayerNameInput() {
    if (!_rendered) {
        console.error('failed to get playerName input - start page is not rendered');
        return;
    }
    if (_PLAYER_NAME_INPUT === null) {
        _PLAYER_NAME_INPUT = getSubmitPlayerNameButton().previousElementSibling;
    }
    return _PLAYER_NAME_INPUT;
}

function getStartPageHTMLContent() {
    return `
<div id="welcome" class="welcome-message">Welcome to the Monopoly game!</div>
<label for="playerNameInput" class="enter-nickname-phrase">Enter your nickname:</label>
<input id="playerNameInput" class="player-name-input" type="text" autocomplete="off" autofocus/>
<button id="submitPlayerName" class="join-game-button">Join the game</button>
    `;
}
