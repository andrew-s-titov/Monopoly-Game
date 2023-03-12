const MAX_PLAYERS = 5;

let _GAME_ROOM_PAGE = null;
let _START_GAME_BUTTON = null;
let _LEAVE_GAME_ROOM_BUTTON = null;
let _CONNECTED_PLAYERS = null;

export function getGameRoomPageElement() {
    if (_GAME_ROOM_PAGE === null) {
        _GAME_ROOM_PAGE = document.createElement('div');
        _GAME_ROOM_PAGE.className = 'game-room-container';
        _GAME_ROOM_PAGE.innerHTML = getGameRoomHTMLContent();
    }
    return _GAME_ROOM_PAGE;
}

export function getStartGameButton() {
    if (_START_GAME_BUTTON === null) {
        _START_GAME_BUTTON = document.getElementById('startGameButton');
    }
    return _START_GAME_BUTTON;
}

export function getLeaveGameRoomButton() {
    if (_LEAVE_GAME_ROOM_BUTTON === null) {
        _LEAVE_GAME_ROOM_BUTTON = document.getElementById('disconnectPlayerButton');
    }
    return _LEAVE_GAME_ROOM_BUTTON;
}

function getConnectedPlayers() {
    if (_CONNECTED_PLAYERS === null) {
        _CONNECTED_PLAYERS = [];
        for (let i = 0; i < MAX_PLAYERS; i++) {
            const playerNameField = document.getElementById(`player${i}`);
            playerNameField.style.textAlign = 'center';
            const playerImage = document.getElementById(`player${i}-image`);
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
            <img id="player0-image" src="/images/user.png" th:src="@{images/user.png}"
                 style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player1-image" src="/images/user.png" th:src="@{images/user.png}"
                 style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player2-image" src="/images/user.png" th:src="@{images/user.png}"
                 style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player3-image" src="/images/user.png" th:src="@{images/user.png}"
                 style="display: none"/>
        </td>
        <td style="width: 200px; height: 250px">
            <img id="player4-image" src="/images/user.png" th:src="@{images/user.png}"
                 style="display: none"/>
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
<button id="startGameButton" class="start-page-button" style="margin-bottom: 20px">Start Game!</button>
<button id="disconnectPlayerButton" class="start-page-button">Leave</button>
    `;
}