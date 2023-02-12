const CONNECTED_PLAYERS = [];
const MAX_PLAYERS = 5;

export async function initializeGameRoom() {
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const playerNameField = document.getElementById(`player${i}`);
        playerNameField.style.textAlign = 'center';
        const playerImage = document.getElementById(`player${i}-image`);
        CONNECTED_PLAYERS.push([playerNameField, playerImage]);
    }
}

export function addToGameRoom(playerName) {
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = CONNECTED_PLAYERS[i];
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
    for (let i = 0; i < MAX_PLAYERS; i++) {
        const player = CONNECTED_PLAYERS[i];
        const playerNameField = player[0];
        const playerImage = player[1];
        if (playerNameField.textContent === playerName) {
            playerNameField.textContent = '';
            playerImage.style.display = 'none';
            break;
        }
    }
}

export function clearGameRoomView() {
    for (let player of CONNECTED_PLAYERS) {
        player[0].textContent = '';
        player[1].style.display = 'none';
    }
}