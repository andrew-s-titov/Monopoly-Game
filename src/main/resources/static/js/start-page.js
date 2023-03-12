let _START_PAGE = null;
let _SUBMIT_PLAYER_NAME_BUTTON = null;
let _PLAYER_NAME_INPUT = null;

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

function getStartPageHTMLContent() {
    return `
<div id="welcome" class="welcome-message">Welcome to the Monopoly game!</div>
<label for="playerNameInput" class="enter-nickname-phrase">Enter your nickname:</label>
<input id="playerNameInput" class="player-name-input" type="text" autocomplete="off" autofocus/>
<button id="submitPlayerName" class="join-game-button">Join the game</button>
    `;
}
