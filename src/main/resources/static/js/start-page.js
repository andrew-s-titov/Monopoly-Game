let _START_PAGE = null;
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

export function getStartPageContainer() {
    if (_START_PAGE === null) {
        _START_PAGE = document.createElement('div');
        _START_PAGE.className = "start-page-container";
        _START_PAGE.innerHTML = getStartPageHTMLContent();
    }
    return _START_PAGE;
}

export function getSubmitPlayerNameButton() {
    if (!_rendered) {
        console.error('failed to get submitPlayerNameButton - start page is not rendered');
        return;
    }
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

function getStartPageHTMLContent() {
    return `
<div id="welcome" class="welcome-message">Welcome to the Monopoly game!</div>
<label for="playerNameInput" class="enter-nickname-phrase">Enter your nickname:</label>
<input id="playerNameInput" class="player-name-input" type="text" autocomplete="off" autofocus/>
<button id="submitPlayerName" class="join-game-button">Join the game</button>
    `;
}
