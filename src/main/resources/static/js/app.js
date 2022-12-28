import {
    addClickEvent, createActionButton, renderActionContainer, removeOldActionContainer,
    renderPropertyManagementContainer, renderGiveUpConfirmation, PROPERTY_MANAGEMENT_PREFIX
} from './buttons.js';
import {renderDiceGifs, renderDiceResult, hideDice, preloadDice} from './dice.js';
import {setHost, getBaseGameUrl, sendGetHttpRequest, sendPostHttpRequest, getBaseWebsocketUrl} from './http.js';
import {renderMortgageState, renderHouses, renderFieldViews} from './field-view.js';
import {
    addPlayers, changePlayerMoney, getPlayerColor, getPlayerIndex, getPlayerName,
    movePlayerChip, bankruptPlayer
} from './players.js';
import {removeReplyWaitingScreen, renderOfferProposal} from './offer.js';

const PLAYER_ID_COOKIE = 'player_id';
const PLAYER_OUTLINE_CLASSNAME = 'player-outline';

let thisPlayerId = null;
let webSocket = null;
let gameInProgress = false;

window.onload = () => {
    let host = document.getElementById('proxy-host').innerText;
    setHost(host);

    let submitPlayerNameButton = document.getElementById('submitPlayerName');
    if (submitPlayerNameButton) addClickEvent(submitPlayerNameButton, () => joinGameRoom());

    let startGameButton = document.getElementById('startGameButton');
    if (startGameButton) addClickEvent(startGameButton, () => startGame());

    let disconnectPlayerButton = document.getElementById('disconnectPlayerButton');
    if (disconnectPlayerButton) addClickEvent(disconnectPlayerButton, () => disconnectPlayer());

    let playerMessageButton = document.getElementById('player-message-button');
    if (playerMessageButton) addClickEvent(playerMessageButton, () => processPlayerMessage());

    let playerMessageInput = document.getElementById('playerNameInput');
    if (playerMessageInput) {
        playerMessageInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                submitPlayerNameButton.click();
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

function joinGameRoom() {
    const playerName = document.getElementById('playerNameInput').value;
    sendGetHttpRequest(`${getBaseGameUrl()}?name=${playerName}`, true,
        function (requester) {
            if (requester.readyState === XMLHttpRequest.DONE) {
                if (requester.status === 200) {
                    openWebsocket(playerName);
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

    webSocket = new WebSocket(`${getBaseWebsocketUrl()}/${username}`);
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
        } else if (socketMessageCode === 101) {
            onPlayerConnected(socketMessage);
        } else if (socketMessageCode === 102) {
            onPlayerDisconnected(socketMessage);
        } else if (socketMessageCode === 200) {
            onChatMessage(socketMessage);
        } else if (socketMessageCode === 201) {
            onSystemMessage(socketMessage);
        } else if (socketMessageCode === 300) {
            onGameStartOrMapRefresh(socketMessage);
        } else if (socketMessageCode === 301) {
            onTurnStart(socketMessage);
        } else if (socketMessageCode === 302) {
            onDiceStartRolling(socketMessage);
        } else if (socketMessageCode === 303) {
            onDiceResult(socketMessage);
        } else if (socketMessageCode === 304) {
            onPlayerChipMove(socketMessage);
        } else if (socketMessageCode === 305) {
            onMoneyChange(socketMessage);
        } else if (socketMessageCode === 306) {
            onBuyProposal(socketMessage);
        } else if (socketMessageCode === 307) {
            renderFieldViews(socketMessage.changes);
        } else if (socketMessageCode === 308) {
            onJailReleaseProcess(socketMessage);
        } else if (socketMessageCode === 309) {
            onAuctionRaiseProposal(socketMessage);
        } else if (socketMessageCode === 310) {
            onAuctionBuyProposal(socketMessage);
        } else if (socketMessageCode === 311) {
            bankruptPlayer(socketMessage.player_id);
        } else if (socketMessageCode === 312) {
            onPayCommand(socketMessage);
        } else if (socketMessageCode === 313) {
            onMortgageChange(socketMessage);
        } else if (socketMessageCode === 314) {
            renderHouses(socketMessage.field, socketMessage.amount);
        } else if (socketMessageCode === 315) {
            onGameOver(socketMessage);
        } else if (socketMessageCode === 316) {
            onDealOffer(socketMessage);
        } else if (socketMessageCode === 317) {
            onOfferProcessed();
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
    const expirationTime = new Date();
    expirationTime.setTime(expirationTime.getTime() + (24 * 60 * 60 * 1000)); // setting expiration in 1 day
    document.cookie = `${PLAYER_ID_COOKIE}=${playerId};expires=${expirationTime.toUTCString()};path=/`;
}

function getPlayerIdFromCookie() {
    let playerCookie = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${PLAYER_ID_COOKIE}=`));
    return playerCookie ? playerCookie.split('=')[1] : null;
}

function startGame() {
    gameInProgress = true;
    sendPostHttpRequest(getBaseGameUrl(), true,
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

function onGameStartOrMapRefresh(gameMapRefreshEvent) {
    document.getElementById('playersBeforeGame').style.display = 'none';
    document.getElementById('map').style.display = 'block';
    document.body.style.backgroundColor = 'darkslategray';

    // TODO: resize image and make grid fit it
    document.getElementById('mapTable').style.backgroundImage = "url('/images/map-back.png')";
    document.getElementById('mapTable').style.backgroundSize = '658px';

    if (thisPlayerId == null) {
        thisPlayerId = getPlayerIdFromCookie();
    } else {
        savePlayerIdToCookie(thisPlayerId);
    }

    const players = gameMapRefreshEvent.players;
    addPlayers(players);

    outlinePlayer(gameMapRefreshEvent.current_player);

    let fieldViews = gameMapRefreshEvent.fields;
    renderFieldViews(fieldViews);
    for (let fieldView of fieldViews) {
        let fieldIndex = fieldView.id;
        applyFieldManagementEvents(fieldIndex);
    }

    // auto-click 'send' button in message box if input is active
    let playerMessageInput = document.getElementById('player-message-input');
    playerMessageInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            document.getElementById('player-message-button').click();
        }
    });

    preloadDice();
}

function onPlayerConnected(playerConnectedEvent) {
    let playerName = playerConnectedEvent.player_name;
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

function onPlayerDisconnected(playerDisconnectedEvent) {
    let playerName = playerDisconnectedEvent.player_name;
    for (let i = 0; i < 5; i++) {
        const playerField = document.getElementById(`player${i}`);
        if (playerField.innerHTML === playerName) {
            playerField.innerHTML = '';
            document.getElementById(`player${i}-image`).style.display = 'none';
            break;
        }
    }
}

function onChatMessage(chatMessageEvent) {
    let playerId = chatMessageEvent.player_id;

    let messageElement = messageDiv();

    let nameText = document.createElement('span');
    nameText.style.color = getPlayerColor(playerId);
    nameText.style.fontWeight = 'bold';
    nameText.innerText = getPlayerName(playerId);

    let messageText = document.createElement('span');
    messageText.innerText = `: ${chatMessageEvent.message}`;

    messageElement.appendChild(nameText);
    messageElement.appendChild(messageText);
    sendMessageToChat(messageElement);
}

function onSystemMessage(systemMessageEvent) {
    let message = systemMessageEvent.message;
    let messageElement = messageDiv();
    messageElement.textContent = message;
    messageElement.style.fontStyle = 'italic';
    sendMessageToChat(messageElement);
}

function messageDiv() {
    let messageElement = document.createElement('div');
    messageElement.className = 'message-body';
    return messageElement;
}

function sendMessageToChat(htmlDivMessage) {
    let messageContainer = document.getElementById('message-container');
    messageContainer.appendChild(htmlDivMessage);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}

function onTurnStart(turnStartEvent) {
    removePlayersOutline();
    let playerToGo = turnStartEvent.player_id;
    outlinePlayer(playerToGo);
    if (thisPlayerId === playerToGo) {

        let throwDiceButton = createActionButton('Roll the dice!', `${getBaseGameUrl()}/dice/notify`, false);
        addClickEvent(throwDiceButton, () => throwDiceButton.remove());
        throwDiceButton.classList.add('roll-the-dice-button', 'flushing');
        document.getElementById('message-container').appendChild(throwDiceButton);
    }
}

function processPlayerMessage() {
    const playerMessageInput = document.getElementById('player-message-input');
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
    document.getElementById(`player${playerIndex}-group`).classList.add(PLAYER_OUTLINE_CLASSNAME);
}

function removePlayersOutline() {
    for (let group of document.getElementsByClassName('player-group')) {
        let groupClassList = group.classList;
        if (groupClassList.contains(PLAYER_OUTLINE_CLASSNAME)) {
            groupClassList.remove(PLAYER_OUTLINE_CLASSNAME);
            return;
        }
    }
}

function onDiceStartRolling(diceRollingStartEvent) {
    renderDiceGifs();
    if (thisPlayerId === diceRollingStartEvent.player_id) {
        setTimeout(() => {
            sendGetHttpRequest(`${getBaseGameUrl()}/dice/roll`, true)
        }, 1500);
    }
}

function onDiceResult(diceResultEvent) {
    renderDiceResult(diceResultEvent.first_dice, diceResultEvent.second_dice);
    setTimeout(() => {
        hideDice();
        if (thisPlayerId === diceResultEvent.player_id) {
            sendGetHttpRequest(`${getBaseGameUrl()}/dice/after`, true);
        }
    }, 2000);
}

function onPlayerChipMove(chipMoveEvent) {
    let playerId = chipMoveEvent.player_id;
    movePlayerChip(playerId, chipMoveEvent.field);
    if (chipMoveEvent.need_after_move_call && playerId === thisPlayerId) {
        setTimeout(() => {
            sendGetHttpRequest(`${getBaseGameUrl()}/after_move`, true);
        }, 500);
    }
}

function onMoneyChange(moneyChangeEvent) {
    for (let change of moneyChangeEvent.changes) {
        changePlayerMoney(change.player_id, change.money);
    }
}

function onBuyProposal(buyProposalEvent) {
    let playerId = buyProposalEvent.player_id;
    let price = buyProposalEvent.price;
    let field = buyProposalEvent.field_name;
    if (thisPlayerId === playerId) {
        let acceptButton = createActionButton('Buy', `${getBaseGameUrl()}/buy?action=ACCEPT`, false);
        let auctionButton = createActionButton('Auction', `${getBaseGameUrl()}/buy?action=DECLINE`, false);
        renderActionContainer(`Do you like to buy ${field} for $${price}?`, acceptButton, auctionButton);
    }
}

function onJailReleaseProcess(jailReleaseProcessEvent) {
    removePlayersOutline();
    let imprisonedPlayer = jailReleaseProcessEvent.player_id;
    outlinePlayer(imprisonedPlayer);
    if (thisPlayerId === imprisonedPlayer) {
        removeOldActionContainer();
        let payButton = createActionButton(`Pay $${jailReleaseProcessEvent.bail}`, `${getBaseGameUrl()}/jail?action=PAY`,
            !jailReleaseProcessEvent.bail_available);
        let luckButton = createActionButton('Try luck', `${getBaseGameUrl()}/jail?action=LUCK`, false);
        renderActionContainer('Chose a way out:', payButton, luckButton);
    }
}

function onAuctionBuyProposal(auctionBuyProposalEvent) {
    let buyButton = createActionButton('Buy', `${getBaseGameUrl()}/auction/buy?action=ACCEPT`, false);
    let declineButton = createActionButton('Decline', `${getBaseGameUrl()}/auction/buy?action=DECLINE`, false);
    renderActionContainer(
        `Do you want to buy ${auctionBuyProposalEvent.field_name} for $${auctionBuyProposalEvent.proposal}?`,
        buyButton, declineButton);
}

function onAuctionRaiseProposal(auctionRaiseProposalEvent) {
    let raiseButton = createActionButton('Raise', `${getBaseGameUrl()}/auction/raise?action=ACCEPT`, false);
    let declineButton = createActionButton('Decline', `${getBaseGameUrl()}/auction/raise?action=DECLINE`, false);
    renderActionContainer(
        `Do you want to raise ${auctionRaiseProposalEvent.field_name} price to $${auctionRaiseProposalEvent.proposal}?`,
        raiseButton, declineButton);
}

function onPayCommand(payCommandEvent) {
    removeOldActionContainer();
    let sum = payCommandEvent.sum;
    let payable = payCommandEvent.payable;
    let wiseToGiveUp = payCommandEvent.wise_to_give_up;
    let payButton = createActionButton('Pay', `${getBaseGameUrl()}/pay`, !payable);
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = createActionButton('Give up');
        addClickEvent(giveUpButton, () => renderGiveUpConfirmation());
    }
    renderActionContainer(`Pay $${sum}`, payButton, giveUpButton);
}

function onMortgageChange(mortgageChangeEvent) {
    let changes = mortgageChangeEvent.changes;
    for (let change of changes) {
        renderMortgageState(change.field, change.turns);
    }
}

function onGameOver(gameOverEvent) {
    let winnerName = gameOverEvent.player_name;

    let winnerInfoContainer = document.createElement('div');
    winnerInfoContainer.className = 'fullscreen-shadow-container';

    let winnerInfo = document.createElement('div');
    winnerInfo.className = 'center-screen-container';
    winnerInfo.innerText = `${winnerName} is the winner!`;

    winnerInfoContainer.appendChild(winnerInfo);
    document.body.appendChild(winnerInfoContainer);

    setTimeout(() => {
        winnerInfoContainer.remove();
        if (webSocket != null && webSocket.readyState !== WebSocket.OPEN) {
            webSocket.close();
            webSocket = null;
        }
        document.getElementById('startPage').style.display = 'block';
        location.reload();
    }, 5000);
}

function applyFieldManagementEvents(fieldIndex) {
    let htmlField = document.getElementById(`field${fieldIndex}`);
    if (!htmlField) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    htmlField.addEventListener('click', (event) => {
        if (event.target.id.startsWith(PROPERTY_MANAGEMENT_PREFIX)) {
            return;
        }
        sendGetHttpRequest(`${getBaseGameUrl()}/field/${fieldIndex}/management`, true,
            function (requester) {
                if (requester.readyState === XMLHttpRequest.DONE && requester.status === 200) {
                    renderPropertyManagementContainer(htmlField, fieldIndex, JSON.parse(requester.response));
                } else {
                    console.error('failed to load available management actions');
                    console.log(requester.response);
                }
            });
    });
}

function onDealOffer(offerProposal) {
    renderOfferProposal(offerProposal);
}

function onOfferProcessed() {
    removeReplyWaitingScreen();
}