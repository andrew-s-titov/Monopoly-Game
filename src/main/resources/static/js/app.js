import {
    createActionButton,
    renderActionContainer,
    removeOldActionContainer
} from "./buttons.js";

import {
    renderDiceGifs,
    diceGifToValues,
    hideDice
} from "./dice.js"

import  {
    renderChip,
    defineChipPosition
} from "./chips.js"

import {
    PLAYER_COLORS,
    GROUP_COLORS
} from "./colors.js"

const PLAYER_ID_COOKIE = 'player_id';

let thisPlayerId = null;
let thisPlayerName = null;
let webSocket = null;
let gameInProgress = false;
let playersMap = new Map();

window.onload = () => {
    document.getElementById('submitPlayerName').onclick = () => joinToRoom();
    document.getElementById('startGameButton').onclick = () => startGame();
    document.getElementById('disconnectPlayerButton').onclick = () => disconnectPlayer();
    document.getElementById('playerMessageButton').onclick = () => processPlayerMessage();

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

    let httpRequester = new XMLHttpRequest();
    httpRequester.onload = () => {
        if (httpRequester.readyState === XMLHttpRequest.DONE) {
            if (httpRequester.status === 200) {
                openWebsocket(playerName);
                thisPlayerName = playerName;
            } else {
                let errorPopUp = document.getElementById('errorMessage');
                errorPopUp.style.display = 'block';
                if (httpRequester.status === 400) {
                    errorPopUp.innerHTML = httpRequester.responseText;
                } else {
                    errorPopUp.innerHTML = 'Unexpected server error';
                }
            }
        }
    };
    httpRequester.onerror = () => httpError('errorMessage');
    httpRequester.open('GET', 'http://localhost:8080/game/name/' + playerName, true);
    httpRequester.send();
}

function openWebsocket(username) {
    document.getElementById('startPage').style.display = 'none';
    document.getElementById('playersBeforeGame').style.display = 'block';

    webSocket = new WebSocket('ws://localhost:8080/connect/' + username);
    webSocket.addEventListener('open', () => {
        console.log('websocket is opened for player ' + username);
    })
    webSocket.addEventListener('close', (event) => {
        console.log('websocket connection is closed');
        location.reload();
    })
    webSocket.addEventListener('message', (message) => {
        const socketMessage = JSON.parse(message.data);
        const socketMessageCode = socketMessage.code;
        console.log('response code is ' + socketMessageCode);
        if (socketMessageCode === 100) {
            console.log('received current user identification event')
            thisPlayerId = socketMessage.player_id;
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
            defineChipPosition(socketMessage.player_id, socketMessage.field);
        }
        if (socketMessageCode === 305) {
            onMoneyChange(socketMessage);
        }
        if (socketMessageCode === 306) {
            onBuyProposal(socketMessage);
        }
        if (socketMessageCode === 307) {
            onPropertyNewOwner(socketMessage);
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
            onFieldViewChange(socketMessage);
        }
        if (socketMessageCode === 312) {
            onPayCommand(socketMessage);
        }
        if (socketMessageCode === 313) {
            onMortgageChange(socketMessage);
        }
        if (socketMessageCode === 314) {
            onStreetHouseAmount(socketMessage);
        }
    })
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
        let expires = 'expires=' + expirationTime.toUTCString();
        document.cookie = PLAYER_ID_COOKIE + '=' + playerId + ';' + expires + '; path = /';
    }
}

function getPlayerIdFromCookie() {
    let playerCookie = document.cookie.split('; ').find((cookie) => cookie.startsWith(PLAYER_ID_COOKIE + '='));
    return playerCookie ? playerCookie.split('=')[1] : null;
}

function startGame() {
    gameInProgress = true;
    let httpRequester = new XMLHttpRequest();
    httpRequester.onload = () => {
        if (httpRequester.readyState === XMLHttpRequest.DONE) {
            if (httpRequester.status !== 200) {
                console.error('Unexpected server response');
                httpError('errorMessage');
            } else {
                // TODO: catch exceptions
            }
        }
    };
    httpRequester.onerror = () => {
        console.error('Server not responding');
        httpError('errorMessage');
    }
    httpRequester.open('POST', 'http://localhost:8080/game', true);
    httpRequester.send();
}

function httpError(elementId) {
    let errorPopUp = document.getElementById(elementId);
    errorPopUp.style.display = 'block';
    errorPopUp.innerHTML = 'Unexpected server error';
}

function onGameStartOrMapRefresh(socketMessage) {
    document.getElementById('playersBeforeGame').style.display = 'none';
    document.getElementById('map').style.display = 'block';
    document.body.style.backgroundColor = 'darkslategray';

    console.log("this player id from script before cookie management: " + thisPlayerId);
    if (thisPlayerId == null) {
        thisPlayerId = getPlayerIdFromCookie();
        console.log("this player id from cookie: " + thisPlayerId);
    } else {
        savePlayerIdToCookie(thisPlayerId);
    }
    console.log("this player id after cookie management: " + thisPlayerId);

    const players = socketMessage.players;
    for (let i = 0; i < players.length; i++) {
        let playerColor = PLAYER_COLORS[i];

        let player = players[i];

        let playerNameField = document.getElementById('player' + i + '-name');
        playerNameField.innerHTML = player.name;
        playerNameField.style.backgroundColor = playerColor;

        let playerMoneyField = document.getElementById('player' + i + '-money');
        playerMoneyField.innerHTML = '$ ' + player.money;
        playerMoneyField.style.backgroundColor = playerColor;

        let playerIcon = document.createElement('img');
        playerIcon.setAttribute('src', 'images/user.png')
        playerIcon.style.width = '90px';
        playerIcon.style.height = '90px';
        let playerIconField = document.getElementById('player' + i + '-icon');
        playerIconField.appendChild(playerIcon);
        playerIconField.style.backgroundColor = playerColor;

        // adding to map server player ID with array ID
        playersMap.set(player.id, [i, player.name]);

        let playerId = player.id;
        renderChip(i, playerId)
        let playerPosition = player.position;
        defineChipPosition(playerId, playerPosition);
    }

    outlinePlayer(socketMessage.current_player);

    let fieldViews = socketMessage.fields;
    renderFieldViews(fieldViews);
    for (let fieldView of fieldViews) {
        let fieldId = fieldView.id;
        let htmlField = document.getElementById('field' + fieldId);
        applyFieldManagementEvents(fieldId, htmlField);
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
    console.log('player ' + playerName + ' connected');
    for (let i = 0; i < 5; i++) {
        let playerField = document.getElementById('player' + i);
        if (playerField.innerHTML.trim() === '') {
            playerField.innerHTML = playerName;
            playerField.style.textAlign = 'center';
            document.getElementById('player' + i + 'Image').style.display = 'block';
            break;
        }
    }
}

function onPlayerDisconnected(socketMessage) {
    let playerName = socketMessage.player_name;
    console.log('player ' + playerName + ' disconnected');
    for (let i = 0; i < 5; i++) {
        const playerField = document.getElementById('player' + i);
        if (playerField.innerHTML === playerName) {
            playerField.innerHTML = '';
            document.getElementById('player' + i + 'Image').style.display = 'none';
            break;
        }
    }
}

function onChatMessage(socketMessage) {
    let message = socketMessage.message;
    let playerId = socketMessage.player_id;
    let playerName = playersMap.get(playerId)[1];
    let playerColor = PLAYER_COLORS[playersMap.get(playerId)[0]];
    // TODO: apply color to player name;
    let messageElement = messageDiv();
    messageElement.textContent = playerName + ': ' + message;
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

        let throwDiceButton = createActionButton('Roll the dice!', 'http://localhost:8080/game/dice/notify', true);
        throwDiceButton.addEventListener('click', () => throwDiceButton.remove());
        document.getElementById('map').appendChild(throwDiceButton);
        throwDiceButton.style.position = 'fixed';
        throwDiceButton.style.left = '47%';
        throwDiceButton.style.top = '50%';
    }
}

function processPlayerMessage() {
    const playerMessageInput = document.getElementById('playerMessage');
    let text = playerMessageInput.value;
    if (text.trim() !== '' && webSocket != null) {
        webSocket.send(text);
        playerMessageInput.value = '';
    }
}

function outlinePlayer(playerId) {
    let playerHtmlId = playersMap.get(playerId)[0];
    document.getElementById('player' + playerHtmlId + '-group').style.outline = '5px solid white';
}

function removePlayersOutline() {
    for (let group of document.getElementsByClassName('player-group')) {
        group.style.outline = 'none';
    }
}

function onDiceResult(socketMessage) {
    diceGifToValues(socketMessage.first_dice, socketMessage.second_dice);
    setTimeout(() => {
        hideDice();

        if (thisPlayerId === socketMessage.player_id) {
            let httpRequester = new XMLHttpRequest();
            httpRequester.open('GET', 'http://localhost:8080/game/dice/after', true);
            httpRequester.send();
        }
    }, 2000);
}

function onMoneyChange(socketMessage) {
    for (let change of socketMessage.changes) {
        let playerId = change.player_id;
        let playerDocumentId = playersMap.get(playerId)[0];
        document.getElementById('player' + playerDocumentId + '-money').innerHTML = '$ ' + change.money;
    }
}

function onBuyProposal(socketMessage) {
    let playerId = socketMessage.player_id;
    let price = socketMessage.price;
    let field = socketMessage.field_name;
    if (thisPlayerId === playerId) {
        let acceptButton = createActionButton('Buy', 'http://localhost:8080/game/buy?action=ACCEPT', true);
        let auctionButton = createActionButton('Auction', 'http://localhost:8080/game/buy?action=DECLINE', true);
        renderActionContainer('Do you like to buy ' + field + ' for $' + price + '?', acceptButton, auctionButton);
    }
}

function onPropertyNewOwner(socketMessage) {
    for (let change of socketMessage.changes) {
        let playerId = change.player_id;
        let fieldId = change.field;
        document.getElementById('field' + fieldId).style.backgroundColor = PLAYER_COLORS[playersMap.get(playerId)[0]];
        // TODO: change font color?
    }
}

function onJailReleaseProcess(socketMessage) {
    removePlayersOutline();
    let imprisonedPlayer = socketMessage.player_id;
    outlinePlayer(imprisonedPlayer);
    if (thisPlayerId === imprisonedPlayer) {
        removeOldActionContainer();
        let payButton = createActionButton('Pay $ ' + socketMessage.bail, 'http://localhost:8080/game/jail?action=PAY',
            socketMessage.bail_available);
        let luckButton = createActionButton('Try luck', 'http://localhost:8080/game/jail?action=LUCK', true);
        renderActionContainer('Chose a way out:', payButton, luckButton);
    }
}

function onAuctionBuyProposal(socketMessage) {
    let buyButton = createActionButton('Buy', 'http://localhost:8080/game/auction/buy?action=ACCEPT', true);
    let declineButton = createActionButton('Decline', 'http://localhost:8080/game/auction/buy?action=DECLINE', true);
    renderActionContainer(
        'Do you want to buy ' + socketMessage.field_name + ' for ' + socketMessage.proposal + ' ?',
        buyButton, declineButton);
}

function onAuctionRaiseProposal(socketMessage) {
    let raiseButton = createActionButton('Raise', 'http://localhost:8080/game/auction/raise?action=ACCEPT', true);
    let declineButton = createActionButton('Decline', 'http://localhost:8080/game/auction/raise?action=DECLINE', true);
    renderActionContainer(
        'Do you want to raise ' + socketMessage.field_name + ' price to $' + socketMessage.proposal + '?',
        raiseButton, declineButton);
}

function onFieldViewChange(socketMessage) {
    renderFieldViews(socketMessage.changes);
}

function onPayCommand(socketMessage) {
    removeOldActionContainer();
    let sum = socketMessage.sum;
    let payable = socketMessage.payable;
    let wiseToGiveUp = socketMessage.wise_to_give_up;
    let payButton = createActionButton('Pay', 'http://localhost:8080/game/pay', payable);
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = createActionButton('Give up', 'http://localhost:8080/game/give_up', true);
    }
    renderActionContainer('Pay $' + sum, payButton, giveUpButton);
}

function onMortgageChange(socketMessage) {
    let changes = socketMessage.changes;
    for (let change of changes) {
        let fieldId = change.field;
        let turns = change.turns;
        renderMortgagePlate(fieldId, turns);
    }
}

function onStreetHouseAmount(socketMessage) {
    let fieldId = socketMessage.field;
    let amount = socketMessage.amount;
    renderHouses(fieldId, amount);
}

function onDiceStartRolling(socketMessage) {
    renderDiceGifs();
    if (thisPlayerId === socketMessage.player_id) {
        setTimeout(diceRollActionRequest, 1500);
    }
}

function diceRollActionRequest() {
    let httpRequester = new XMLHttpRequest();
    httpRequester.open('GET', 'http://localhost:8080/game/dice/roll', true);
    httpRequester.send();
}

function renderFieldViews(fieldViews) {
    for (let fieldView of fieldViews) {
        let fieldId = fieldView.id;
        let htmlField = document.getElementById('field' + fieldId);
        let nameField = document.getElementById('field' + fieldId + '-name');
        if (nameField) {
            nameField.innerHTML = fieldView.name;
        } else {
            htmlField.innerHTML = fieldView.name;
        }
        if (fieldView.hasOwnProperty('owner_id')) {
            htmlField.style.backgroundColor = PLAYER_COLORS[playersMap.get(fieldView.owner_id)[0]];
        } else {
            htmlField.style.backgroundColor = 'white';
        }
        if (fieldView.hasOwnProperty('price_tag')) {
            let priceTagField = document.getElementById('field' + fieldId + '-price');
            let priceTag = fieldView.price_tag;
            priceTagField.innerHTML = priceTag;
            priceTagField.style.backgroundColor = GROUP_COLORS[fieldView.group];
            if (fieldView.mortgage) {
                renderMortgagePlate(fieldId, priceTag);
            }
        }
        if (fieldView.hasOwnProperty('houses')) {
            let houses = fieldView.houses;
            renderHouses(fieldId, houses);
        }
    }
}

function applyFieldManagementEvents(fieldId, htmlField) {
    htmlField.addEventListener('click', (event) => {
        if (event.target.id.startsWith('management')) {
            return;
        }
        let httpRequester = new XMLHttpRequest();
        httpRequester.open('GET', 'http://localhost:8080/game/field/' + fieldId + '/management', true);
        httpRequester.onload = () => {
            if (httpRequester.readyState === XMLHttpRequest.DONE && httpRequester.status === 200) {
                let jsonResponse = JSON.parse(httpRequester.response);
                console.log('management response: \n' + jsonResponse);
                if (jsonResponse.length > 0) {
                    let managementContainer = document.createElement("div");
                    managementContainer.id = 'management_container';
                    managementContainer.style.position = 'absolute';
                    managementContainer.style.width = '100%';
                    managementContainer.style.height = '100%';
                    managementContainer.style.opacity = '0.8';
                    managementContainer.style.background = 'black';
                    managementContainer.style.writingMode = '';
                    managementContainer.style.transform = '';
                    document.addEventListener('click', (event) => {
                        if (event.target.id !== 'management_container') {
                            managementContainer.remove();
                        }
                    });
                    for (let action of jsonResponse) {
                        let button = document.createElement('button');
                        button.style.width = '100%';
                        button.style.color = 'black';
                        button.style.fontSize = '10px';
                        if (action === 'INFO') {
                            // TODO: show info card atop of everything;
                            button.innerHTML = 'Info';
                            button.id = 'management-info';
                            button.addEventListener('click', () => alert('info should be here'));
                        } else if (action === 'MORTGAGE') {
                            button.innerHTML = 'Mortgage';
                            button.id = 'management-mortgage';
                            button.addEventListener('click', () => {
                                let httpRequester = new XMLHttpRequest();
                                httpRequester.open('GET', 'http://localhost:8080/game/field/' + fieldId + '/mortgage', true);
                                httpRequester.send();
                            });
                        } else if (action === 'REDEEM') {
                            button.innerHTML = 'Redeem';
                            button.id = 'management-redeem';
                            button.addEventListener('click', () => {
                                let httpRequester = new XMLHttpRequest();
                                httpRequester.open('GET', 'http://localhost:8080/game/field/' + fieldId + '/redeem', true);
                                httpRequester.send();
                            });
                        } else if (action === 'BUY_HOUSE') {
                            button.innerHTML = 'Buy a house';
                            button.id = 'management-buy-house';
                            button.addEventListener('click', () => {
                                let httpRequester = new XMLHttpRequest();
                                httpRequester.open('GET', 'http://localhost:8080/game/field/' + fieldId + '/buy_house', true);
                                httpRequester.send();
                            });
                        } else if (action === 'SELL_HOUSE') {
                            button.innerHTML = 'Sell a house';
                            button.id = 'management-sell-house';
                            button.addEventListener('click', () => {
                                let httpRequester = new XMLHttpRequest();
                                httpRequester.open('GET', 'http://localhost:8080/game/field/' + fieldId + '/sell_house', true);
                                httpRequester.send();
                            });
                        }
                        button.addEventListener('click', () => managementContainer.remove());
                        managementContainer.appendChild(button);
                    }
                    htmlField.appendChild(managementContainer);
                }
            } else {
                console.log(httpRequester.response);
            }
        };
        httpRequester.send();
    });
}

function renderMortgagePlate(fieldId, turns) {
    let mortgagePlateId = 'field' + fieldId + '-mortgage';
    let oldMortgagePlate = document.getElementById(mortgagePlateId);
    if (oldMortgagePlate) {
        oldMortgagePlate.remove();
    }
    if (turns > 0) {
        let fieldPriceField = document.getElementById('field' + fieldId + '-price');
        if (fieldPriceField) {
            fieldPriceField.innerText = turns;
        }
        let newMortgagePlate = document.createElement('div');
        newMortgagePlate.id = mortgagePlateId;
        newMortgagePlate.style.width = '100%';
        newMortgagePlate.style.height = '100%';
        newMortgagePlate.style.position = 'absolute';
        newMortgagePlate.style.background = 'white';
        newMortgagePlate.style.opacity = '0.8';
        newMortgagePlate.innerText = 'MORTGAGE';
        newMortgagePlate.style.color = 'red';
        newMortgagePlate.style.fontSize = '8px';
        newMortgagePlate.style.fontWeight = 'bold';
        newMortgagePlate.style.textAlign = 'center';
        document.getElementById('field' + fieldId).appendChild(newMortgagePlate);
    }
}

function renderHouses(fieldId, amount) {
    let field = document.getElementById('field' + fieldId);
    let houseContainerId = 'field' + fieldId + '-houses';
    let oldContainer = document.getElementById(houseContainerId);
    if (oldContainer) {
        oldContainer.remove();
    }
    if (amount > 0) {
        let houseContainer = document.createElement('div');
        houseContainer.id = houseContainerId;
        houseContainer.style.position = 'absolute';
        if (fieldId < 20) {
            houseContainer.style.up = '0px';
            houseContainer.style.right = '0px';
        } else {
            houseContainer.style.bottom = '0px';
            houseContainer.style.left = '0px';
        }
        if (amount === 5) {
            let hotel = document.createElement('img');
            hotel.setAttribute('src', 'images/hotel.png')
            hotel.style.width = '20px';
            hotel.style.height = '20px';
            houseContainer.appendChild(hotel);
        } else {
            for (let i = 0; i < amount; i++) {
                let house = document.createElement('img');
                house.setAttribute('src', 'images/house.png')
                house.style.width = '10px';
                house.style.height = '10px';
                houseContainer.appendChild(house);
            }
        }
        field.appendChild(houseContainer);
    }
}