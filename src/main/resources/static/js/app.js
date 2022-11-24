import {
    addClickEvent,
    createActionButton,
    renderActionContainer,
    removeOldActionContainer,
    renderPropertyManagementContainer,
    PROPERTY_MANAGEMENT_BASE_URL
} from './buttons.js';
import {renderDiceGifs, renderDiceResult, hideDice} from './dice.js'
import {sendGetHttpRequest, sendPostHttpRequest} from './http.js'
import {renderMortgagePlate, renderHouses, renderFieldViews} from './field-view.js';
import {addPlayers, changePlayerMoney, getPlayerColor, getPlayerIndex, getPlayerName, movePlayerChip} from "./players.js";

const PLAYER_ID_COOKIE = 'player_id';
const HOST_AND_PORT = 'localhost:8080';
const BASE_GAME_URL = `http://${HOST_AND_PORT}/game`

let thisPlayerId = null;
let thisPlayerName = null;
let webSocket = null;
let gameInProgress = false;

window.onload = () => {
    addClickEvent(document.getElementById('submitPlayerName'), () => joinToRoom());
    addClickEvent(document.getElementById('startGameButton'), () => startGame());
    addClickEvent(document.getElementById('disconnectPlayerButton'), () => disconnectPlayer());
    addClickEvent(document.getElementById('playerMessageButton'), () => processPlayerMessage());

    let playerMessageInput = document.getElementById('playerNameInput');
    if (playerMessageInput) {
        playerMessageInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                document.getElementById('submitPlayerName').click();
            }
        });
    }
    let reconnect = document.getElementById('reconnect');
    if (reconnect) {
        if (webSocket == null || webSocket.readyState === WebSocket.CLOSED) {
            openWebsocket(reconnect.innerText);
        } else {
            console.warn('websocket is not closed!')
        }
    }
};

function joinToRoom() {
    const playerName = document.getElementById('playerNameInput').value;
    sendGetHttpRequest(`${BASE_GAME_URL}/name/${playerName}`, true,
        function (requester) {
            if (requester.readyState === XMLHttpRequest.DONE) {
                if (requester.status === 200) {
                    openWebsocket(playerName);
                    thisPlayerName = playerName;
                } else {
                    let errorPopUp = document.getElementById('errorMessage');
                    errorPopUp.style.display = 'block';
                    if (requester.status === 400) {
                        errorPopUp.innerHTML = requester.responseText;
                    } else {
                        errorPopUp.innerHTML = 'Unexpected server error';
                    }
                }
            }
        },
        function (requester) {
            httpError('errorMessage');
        }
    );
}

function openWebsocket(username) {
    document.getElementById('startPage').style.display = 'none';
    document.getElementById('playersBeforeGame').style.display = 'block';

    webSocket = new WebSocket(`ws://${HOST_AND_PORT}/connect/${username}`);
    webSocket.onclose = (event) => {
        console.log('websocket connection is closed');
        location.reload();
    };
    webSocket.onmessage = (message) => {
        const socketMessage = JSON.parse(message.data);
        const socketMessageCode = socketMessage.code;
        console.log(`websocket event code is ${socketMessageCode}`);
        if (socketMessageCode === 100) {
            let playerId = socketMessage.player_id;
            console.log(`received current user identification event, id: ${playerId}`);
            thisPlayerId = playerId;
        }
        if (socketMessageCode === 101) {
            onPlayerConnected(socketMessage);
        }
        if (socketMessageCode === 102) {
            onPlayerDisconnected(socketMessage);
        }
        if (socketMessageCode === 200) {
            onChatMessage(socketMessage);
        }
        if (socketMessageCode === 201) {
            onSystemMessage(socketMessage);
        }
        if (socketMessageCode === 300) {
            onGameStartOrMapRefresh(socketMessage);
        }
        if (socketMessageCode === 301) {
            onTurnStart(socketMessage);
        }
        if (socketMessageCode === 302) {
            onDiceStartRolling(socketMessage);
        }
        if (socketMessageCode === 303) {
            onDiceResult(socketMessage);
        }
        if (socketMessageCode === 304) {
            movePlayerChip(socketMessage.player_id, socketMessage.field);
        }
        if (socketMessageCode === 305) {
            onMoneyChange(socketMessage);
        }
        if (socketMessageCode === 306) {
            onBuyProposal(socketMessage);
        }
        if (socketMessageCode === 307) {
            renderFieldViews(socketMessage.changes);
        }
        if (socketMessageCode === 308) {
            onJailReleaseProcess(socketMessage);
        }
        if (socketMessageCode === 309) {
            onAuctionRaiseProposal(socketMessage);
        }
        if (socketMessageCode === 310) {
            onAuctionBuyProposal(socketMessage);
        }
        if (socketMessageCode === 311) {

        }
        if (socketMessageCode === 312) {
            onPayCommand(socketMessage);
        }
        if (socketMessageCode === 313) {
            onMortgageChange(socketMessage);
        }
        if (socketMessageCode === 314) {
            renderHouses(socketMessage.field, socketMessage.amount);
        }
    };
}

function disconnectPlayer() {
    if (webSocket != null) {
        // custom code to parse on server side
        webSocket.close(1000, 'GOING_AWAY');
    }
    webSocket = null;
    location.reload();
}

function savePlayerIdToCookie(playerId) {
    let cookieValue = getPlayerIdFromCookie();
    if (cookieValue == null || cookieValue !== playerId) {
        const expirationTime = new Date();
        expirationTime.setTime(expirationTime.getTime() + (24 * 60 * 60 * 1000)); // setting expiration in 1 day
        document.cookie = `${PLAYER_ID_COOKIE}=${playerId};expires=${expirationTime.toUTCString()};path = /`;
    }
}

function getPlayerIdFromCookie() {
    let playerCookie = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${PLAYER_ID_COOKIE}=`));
    return playerCookie ? playerCookie.split('=')[1] : null;
}

function startGame() {
    gameInProgress = true;
    sendPostHttpRequest(BASE_GAME_URL, true,
        function (requester) {
            if (requester.readyState === XMLHttpRequest.DONE) {
                if (requester.status !== 200) {
                    console.error('Unexpected server response');
                    httpError('errorMessage');
                } else {
                    // TODO: catch exceptions
                }
            }
        },
        function (requester) {
            console.error('Server not responding');
            httpError('errorMessage');
        }
    );
}

function httpError(errorHtmlElementId) {
    let errorPopUp = document.getElementById(errorHtmlElementId);
    if (errorPopUp) {
        errorPopUp.style.display = 'block';
        errorPopUp.innerHTML = 'Unexpected server error';
    } else {
        console.error(`cannot find html element with id ${errorHtmlElementId}`);
    }
}

function onGameStartOrMapRefresh(socketMessage) {
    document.getElementById('playersBeforeGame').style.display = 'none';
    document.getElementById('map').style.display = 'block';
    document.body.style.backgroundColor = 'darkslategray';

    if (thisPlayerId == null) {
        thisPlayerId = getPlayerIdFromCookie();
    } else {
        savePlayerIdToCookie(thisPlayerId);
    }

    const players = socketMessage.players;
    addPlayers(players);

    outlinePlayer(socketMessage.current_player);

    let fieldViews = socketMessage.fields;
    renderFieldViews(fieldViews);
    for (let fieldView of fieldViews) {
        let fieldIndex = fieldView.id;
        applyFieldManagementEvents(fieldIndex);
    }

    // auto-click 'send' button in message box if input is active
    let playerMessageInput = document.getElementById('playerMessage');
    playerMessageInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            document.getElementById('playerMessageButton').click();
        }
    });
}

function onPlayerConnected(socketMessage) {
    let playerName = socketMessage.player_name;
    for (let i = 0; i < 5; i++) {
        let playerField = document.getElementById(`player${i}`);
        if (playerField.innerHTML.trim() === '') {
            playerField.innerHTML = playerName;
            playerField.style.textAlign = 'center';
            document.getElementById(`player${i}-image`).style.display = 'block';
            break;
        }
    }
}

function onPlayerDisconnected(socketMessage) {
    let playerName = socketMessage.player_name;
    for (let i = 0; i < 5; i++) {
        const playerField = document.getElementById(`player${i}`);
        if (playerField.innerHTML === playerName) {
            playerField.innerHTML = '';
            document.getElementById(`player${i}-image`).style.display = 'none';
            break;
        }
    }
}

function onChatMessage(socketMessage) {
    let playerId = socketMessage.player_id;

    let messageElement = messageDiv();

    let nameText = document.createElement('span');
    nameText.style.color = getPlayerColor(playerId);
    nameText.innerText = getPlayerName(playerId);

    let messageText = document.createElement('span');
    messageText.innerText = `: ${socketMessage.message}`;

    messageElement.appendChild(nameText);
    messageElement.appendChild(messageText);
    sendMessageToChat(messageElement);
}

function onSystemMessage(socketMessage) {
    let message = socketMessage.message;
    let messageElement = messageDiv();
    messageElement.textContent = message;
    messageElement.style.fontStyle = 'italic';
    sendMessageToChat(messageElement);
}

function messageDiv() {
    let messageElement = document.createElement('div');
    messageElement.style.textAlign = 'left';
    messageElement.style.color = 'white';
    messageElement.style.fontSize = '15px';
    messageElement.style.padding = '5px';
    messageElement.style.paddingLeft = '10px';
    return messageElement;
}

function sendMessageToChat(htmlDivMessage) {
    let messageContainer = document.getElementById('messageContainer');
    messageContainer.appendChild(htmlDivMessage);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}

function onTurnStart(socketMessage) {
    removePlayersOutline();
    let playerToGo = socketMessage.player_id;
    outlinePlayer(playerToGo);
    if (thisPlayerId === playerToGo) {

        let throwDiceButton = createActionButton('Roll the dice!', `${BASE_GAME_URL}/dice/notify`, true);
        addClickEvent(throwDiceButton, () => throwDiceButton.remove());
        document.getElementById('map').appendChild(throwDiceButton);
        throwDiceButton.style.position = 'fixed';
        throwDiceButton.style.left = '47%';
        throwDiceButton.style.top = '50%';
    }
}

function processPlayerMessage() {
    const playerMessageInput = document.getElementById('playerMessage');
    if (playerMessageInput) {
        let text = playerMessageInput.value;
        if (text.trim() !== '' && webSocket != null) {
            webSocket.send(text);
            playerMessageInput.value = '';
        }
    }
}

function outlinePlayer(playerId) {
    let playerIndex = getPlayerIndex(playerId);
    document.getElementById(`player${playerIndex}-group`).style.outline = '5px solid white';
}

function removePlayersOutline() {
    for (let group of document.getElementsByClassName('player-group')) {
        group.style.outline = 'none';
    }
}

function onDiceResult(socketMessage) {
    renderDiceResult(socketMessage.first_dice, socketMessage.second_dice);
    setTimeout(() => {
        hideDice();
        if (thisPlayerId === socketMessage.player_id) {
            sendGetHttpRequest(`${BASE_GAME_URL}/dice/after`, true);
        }
    }, 2000);
}

function onMoneyChange(socketMessage) {
    for (let change of socketMessage.changes) {
        changePlayerMoney(change.player_id, change.money);
    }
}

function onBuyProposal(socketMessage) {
    let playerId = socketMessage.player_id;
    let price = socketMessage.price;
    let field = socketMessage.field_name;
    if (thisPlayerId === playerId) {
        let acceptButton = createActionButton('Buy', `${BASE_GAME_URL}/buy?action=ACCEPT`, true);
        let auctionButton = createActionButton('Auction', `${BASE_GAME_URL}/buy?action=DECLINE`, true);
        renderActionContainer(`Do you like to buy ${field} for $${price}?`, acceptButton, auctionButton);
    }
}

function onJailReleaseProcess(socketMessage) {
    removePlayersOutline();
    let imprisonedPlayer = socketMessage.player_id;
    outlinePlayer(imprisonedPlayer);
    if (thisPlayerId === imprisonedPlayer) {
        removeOldActionContainer();
        let payButton = createActionButton(`Pay $${socketMessage.bail}`, `${BASE_GAME_URL}/jail?action=PAY`,
            socketMessage.bail_available);
        let luckButton = createActionButton('Try luck', `${BASE_GAME_URL}/jail?action=LUCK`, true);
        renderActionContainer('Chose a way out:', payButton, luckButton);
    }
}

function onAuctionBuyProposal(socketMessage) {
    let buyButton = createActionButton('Buy', `${BASE_GAME_URL}/auction/buy?action=ACCEPT`, true);
    let declineButton = createActionButton('Decline', `${BASE_GAME_URL}/auction/buy?action=DECLINE`, true);
    renderActionContainer(
        `Do you want to buy ${socketMessage.field_name} for $${socketMessage.proposal}?`,
        buyButton, declineButton);
}

function onAuctionRaiseProposal(socketMessage) {
    let raiseButton = createActionButton('Raise', `${BASE_GAME_URL}/auction/raise?action=ACCEPT`, true);
    let declineButton = createActionButton('Decline', `${BASE_GAME_URL}/auction/raise?action=DECLINE`, true);
    renderActionContainer(
        `Do you want to raise ${socketMessage.field_name} price to $${socketMessage.proposal}?`,
        raiseButton, declineButton);
}

function onPayCommand(socketMessage) {
    removeOldActionContainer();
    let sum = socketMessage.sum;
    let payable = socketMessage.payable;
    let wiseToGiveUp = socketMessage.wise_to_give_up;
    let payButton = createActionButton('Pay', `${BASE_GAME_URL}/pay`, payable);
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = createActionButton('Give up', `${BASE_GAME_URL}/give_up`, true);
    }
    renderActionContainer(`Pay $${sum}`, payButton, giveUpButton);
}

function onMortgageChange(socketMessage) {
    let changes = socketMessage.changes;
    for (let change of changes) {
        renderMortgagePlate(change.field, change.turns);
    }
}

function onDiceStartRolling(socketMessage) {
    renderDiceGifs();
    if (thisPlayerId === socketMessage.player_id) {
        setTimeout(() => {
            sendGetHttpRequest(`${BASE_GAME_URL}/dice/roll`, true)
        }, 1500);
    }
}

function applyFieldManagementEvents(fieldIndex) {
    let htmlField = document.getElementById(`field${fieldIndex}`);
    if (!htmlField) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    htmlField.addEventListener('click', (event) => {
        if (event.target.id.startsWith('management')) {
            return;
        }
        sendGetHttpRequest(`${PROPERTY_MANAGEMENT_BASE_URL}/${fieldIndex}/management`, true,
            function (requester) {
                if (requester.readyState === XMLHttpRequest.DONE && requester.status === 200) {
                    let managementActions = JSON.parse(requester.response);
                    if (managementActions.length > 0) {
                        renderPropertyManagementContainer(htmlField, fieldIndex, managementActions);
                    }
                } else {
                    console.error('failed to load available management actions');
                    console.log(requester.response);
                }
            });
    });
}