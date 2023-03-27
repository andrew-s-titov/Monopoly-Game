const MAX_PLAYERS = 5;
const PLAYER_IMAGE_URL = "url('/images/user.png')";

let _GAME_ROOM_PAGE = null;
let _START_GAME_BUTTON = null;
let _LEAVE_GAME_ROOM_BUTTON = null;
let _CONNECTED_PLAYERS = null;

let _rendered = false;

export function isRendered() {
    return _rendered;
}

export function render(parentContainer) {
    if (_rendered) {
        return;
    }
    parentContainer.innerHTML = '';
    parentContainer.appendChild(getGameRoomPageContainer());
    _rendered = true;
}

export function hide(parentContainer) {
    if (!_rendered) {
        return;
    }
    parentContainer.innerHTML = '';
    clear();
    _rendered = false;
}

export function getGameRoomPageContainer() {
    if (_GAME_ROOM_PAGE === null) {
        _GAME_ROOM_PAGE = document.createElement('div');
        _GAME_ROOM_PAGE.className = 'game-room-container';
        _GAME_ROOM_PAGE.innerHTML = getGameRoomHTMLContent();
    }
    return _GAME_ROOM_PAGE;
}

export function getStartGameButton() {
    if (!_rendered) {
        console.error('failed to get startGame button - game room page is not rendered');
        return;
    }
    if (_START_GAME_BUTTON === null) {
        _START_GAME_BUTTON = getLeaveGameRoomButton().previousElementSibling;
    }
    return _START_GAME_BUTTON;
}

export function getLeaveGameRoomButton() {
    if (!_rendered) {
        console.error('failed to get leaveGameRoom button - game room page is not rendered');
        return;
    }
    if (_LEAVE_GAME_ROOM_BUTTON === null) {
        _LEAVE_GAME_ROOM_BUTTON = getGameRoomPageContainer().lastElementChild;
    }
    return _LEAVE_GAME_ROOM_BUTTON;
}

function getConnectedPlayers() {
    if (_CONNECTED_PLAYERS === null) {
        _CONNECTED_PLAYERS = [];
        const images = document.body.getElementsByClassName('image-row')[0].children;
        const names = document.body.getElementsByClassName('name-row')[0].children;
        for (let i = 0; i < MAX_PLAYERS; i++) {
            const playerNameField = names[i];
            const playerImage = images[i];
            _CONNECTED_PLAYERS.push({name: playerNameField, image: playerImage});
        }
    }
    return _CONNECTED_PLAYERS;
}

export function addToGameRoom(playerName) {
    const connectedPlayers = getConnectedPlayers();
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = connectedPlayers[i];
        const playerNameField = player.name;
        const playerImage = player.image;
        if (playerNameField.textContent.trim() === '') {
            playerNameField.textContent = playerName;
            playerImage.style.backgroundImage = PLAYER_IMAGE_URL;
            break;
        }
    }
}

export function removeFromGameRoom(playerName) {
    const connectedPlayers = getConnectedPlayers();
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = connectedPlayers[i];
        const playerNameField = player.name;
        if (playerNameField.textContent === playerName) {
            playerNameField.textContent = '';
            player.image.style.backgroundImage = 'none';
            break;
        }
    }
}

export function clear() {
    const connectedPlayers = getConnectedPlayers();
    for (let player of connectedPlayers) {
        player.name.textContent = '';
        player.image.style.backgroundImage = 'none';
    }
}

function getGameRoomHTMLContent() {
    return `
    <p class="gr-header">Registered players:</p>
    <div class="image-row">
        <div class="gr-player-image"></div>
        <div class="gr-player-image"></div>
        <div class="gr-player-image"></div>
        <div class="gr-player-image"></div>
        <div class="gr-player-image"></div>
    </div>
    <div class="name-row">
        <div class="gr-player-name"></div>
        <div class="gr-player-name"></div>
        <div class="gr-player-name"></div>
        <div class="gr-player-name"></div>
        <div class="gr-player-name"></div>
    </div>
<button class="gr-button">Start Game!</button>
<button class="gr-button">Leave</button>
    `;
}