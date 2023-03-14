const MAX_PLAYERS = 5;

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
            playerNameField.style.textAlign = 'center';
            const playerImage = images[i].firstElementChild;
            _CONNECTED_PLAYERS.push([playerNameField, playerImage]);
        }
    }
    return _CONNECTED_PLAYERS;
}

export function addToGameRoom(playerName) {
    const connectedPlayers = getConnectedPlayers();
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = connectedPlayers[i];
        const playerNameField = player[0];
        const playerImage = player[1];
        if (playerNameField.textContent.trim() === '') {
            playerNameField.textContent = playerName;
            playerImage.style.display = 'block';
            break;
        }
    }
}

export function removeFromGameRoom(playerName) {
    const connectedPlayers = getConnectedPlayers();
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = connectedPlayers[i];
        const playerNameField = player[0];
        if (playerNameField.textContent === playerName) {
            playerNameField.textContent = '';
            player[1].style.display = 'none';
            break;
        }
    }
}

export function clear() {
    const connectedPlayers = getConnectedPlayers();
    for (let player of connectedPlayers) {
        player[0].textContent = '';
        player[1].style.display = 'none';
    }
}

function getGameRoomHTMLContent() {
    return `
    <p style="text-align:center; font-weight: bold; font-size: 25px">Registered players:</p>
<table style="margin-left:auto; margin-right:auto; border-collapse: collapse">
    <tr class="image-row">
        <td style="width: 200px; height: 250px">
            <img id="player0-image" src="/images/user.png" style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player1-image" src="/images/user.png" style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player2-image" src="/images/user.png" style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player3-image" src="/images/user.png" style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player4-image" src="/images/user.png" style="display: none"/>
        </td>
    </tr>
    <tr class="name-row">
        <td id="player0" class="player-name-holder"></td>
        <td id="player1" class="player-name-holder"></td>
        <td id="player2" class="player-name-holder"></td>
        <td id="player3" class="player-name-holder"></td>
        <td id="player4" class="player-name-holder"></td>
    </tr>
</table>
<br>
<button id="startGameButton" class="game-room-button" style="margin-bottom: 20px">Start Game!</button>
<button id="disconnectPlayerButton" class="game-room-button">Leave</button>
    `;
}