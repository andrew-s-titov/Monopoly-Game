import {
    addClickEvent,
    createActionButton,
    renderThrowDiceButton,
    PROPERTY_MANAGEMENT_PREFIX,
    removeOldActionContainer,
    renderActionContainer,
    renderGiveUpConfirmation,
    renderPropertyManagementContainer
} from './buttons.js';
import {hideDice, preloadDice, renderDiceGifs, renderDiceResult} from './dice.js';
import {getBaseGameUrl, getBaseWebsocketUrl, sendGetHttpRequest, sendPostHttpRequest, setHost} from './http.js';
import {renderFieldViews, renderHouses, renderMortgageState} from './field-view.js';
import {
    addPlayers,
    bankruptPlayer,
    changePlayerMoney,
    getPlayerColorById,
    getPlayerIndexById,
    getPlayerNameById,
    movePlayerChip
} from './players.js';
import {removeReplyWaitingScreen, renderOfferProposal} from './offer.js';

const PLAYER_ID_COOKIE = 'player_id';
const RUNNING_CIRCLE_OUTLINE_CLASSNAME = 'running-circle';
const RUNNING_CIRCLE_OUTLINE_ID = 'running-circle';

let thisPlayerId = null;
let webSocket = null;
let gameInProgress = false;

let startBackgroundImageWidth;
let startBackgroundImageHeight;

window.onload = () => {
    const host = document.getElementById('proxy-host').innerText;
    setHost(host);

    const playerMessageButton = document.getElementById('player-message-button');
    if (playerMessageButton) addClickEvent(playerMessageButton, () => processPlayerMessage());

    const reconnect = document.getElementById('reconnect');
    if (reconnect) {
        if (webSocket == null || webSocket.readyState === WebSocket.CLOSED) {
            document.getElementById('startPage').style.display = 'none';
            openWebsocket(reconnect.innerText);
        } else {
            console.warn('websocket is not closed!')
        }
        return;
    }

    initialStartPageBackgroundSet();
    window.addEventListener('resize', resizeBackgroundImage);

    const submitPlayerNameButton = document.getElementById('submitPlayerName');
    if (submitPlayerNameButton) addClickEvent(submitPlayerNameButton, () => joinGameRoom());

    const startGameButton = document.getElementById('startGameButton');
    if (startGameButton) addClickEvent(startGameButton, () => startGame());

    const disconnectPlayerButton = document.getElementById('disconnectPlayerButton');
    if (disconnectPlayerButton) addClickEvent(disconnectPlayerButton, () => disconnectPlayer());

    const playerMessageInput = document.getElementById('playerNameInput');
    if (playerMessageInput) {
        playerMessageInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                submitPlayerNameButton.click();
            }
        });
    }
};

function joinGameRoom() {
    const playerName = document.getElementById('playerNameInput').value;
    sendGetHttpRequest(`${getBaseGameUrl()}?name=${playerName}`, true,
        function (requester) {
            if (requester.readyState === XMLHttpRequest.DONE) {
                if (requester.status === 200) {
                    document.getElementById('startPage').style.display = 'none';
                    document.getElementById('playersBeforeGame').style.display = 'block';
                    openWebsocket(playerName);
                } else {
                    const errorPopUp = document.getElementById('errorMessage');
                    errorPopUp.style.display = 'block';
                    if (requester.status === 400) {
                        errorPopUp.textContent = requester.responseText;
                    } else {
                        errorPopUp.textContent = 'Unexpected server error';
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
    webSocket = new WebSocket(`${getBaseWebsocketUrl()}/${username}`);
    webSocket.onclose = (event) => {
        console.log('websocket connection is closed');
        if (event.code === 3000) {
            reloadPageOnGameOver();
        } else {
            location.reload();
        }
    };
    webSocket.onmessage = (message) => {
        const socketMessage = JSON.parse(message.data);
        const socketMessageCode = socketMessage.code;
        console.log(`websocket event code is ${socketMessageCode}`);
        if (socketMessageCode === 101) {
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
            onPlayerBankrupt(socketMessage);
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

function getPlayerIdFromCookie() {
    const playerCookie = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${PLAYER_ID_COOKIE}=`));
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
    const errorPopUp = document.getElementById(errorHtmlElementId);
    if (errorPopUp) {
        errorPopUp.style.display = 'block';
        errorPopUp.textContent = 'Unexpected server error';
    } else {
        console.error(`cannot find html element with id ${errorHtmlElementId}`);
    }
}

function onGameStartOrMapRefresh(gameMapRefreshEvent) {
    hideStartElements();
    document.getElementById('map').style.display = 'block';
    document.body.style.backgroundColor = 'darkslategray';

    // TODO: resize image and make grid fit it
    document.getElementById('mapTable').style.backgroundImage = "url('/images/map-back.png')";
    document.getElementById('mapTable').style.backgroundSize = '658px';

    const players = gameMapRefreshEvent.players;
    addPlayers(players);

    outlinePlayer(gameMapRefreshEvent.current_player);

    const fieldViews = gameMapRefreshEvent.fields;
    renderFieldViews(fieldViews);
    for (let fieldView of fieldViews) {
        applyFieldManagementEvents(fieldView.id);
    }

    // auto-click 'send' button in message box if input is active
    const playerMessageInput = document.getElementById('player-message-input');
    playerMessageInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            document.getElementById('player-message-button').click();
        }
    });

    preloadDice();
}

function onPlayerConnected(playerConnectedEvent) {
    const playerName = playerConnectedEvent.player_name;
    for (let i = 0; i < 5; i++) {
        const playerField = document.getElementById(`player${i}`);
        if (playerField.textContent.trim() === '') {
            playerField.textContent = playerName;
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
        if (playerField.textContent === playerName) {
            playerField.textContent = '';
            document.getElementById(`player${i}-image`).style.display = 'none';
            break;
        }
    }
}

function onChatMessage(chatMessageEvent) {
    const playerId = chatMessageEvent.player_id;
    const messageElement = messageDiv();

    const nameText = document.createElement('span');
    nameText.style.color = getPlayerColorById(playerId);
    nameText.style.fontWeight = 'bold';
    nameText.innerText = getPlayerNameById(playerId);

    const messageText = document.createElement('span');
    messageText.textContent = `: ${chatMessageEvent.message}`;

    messageElement.appendChild(nameText);
    messageElement.appendChild(messageText);
    sendMessageToChat(messageElement);
}

function onSystemMessage(systemMessageEvent) {
    const message = systemMessageEvent.message;
    const messageElement = messageDiv();
    messageElement.textContent = message;
    messageElement.style.fontStyle = 'italic';
    sendMessageToChat(messageElement);
}

function messageDiv() {
    const messageElement = document.createElement('div');
    messageElement.className = 'message-body';
    return messageElement;
}

function sendMessageToChat(htmlDivMessage) {
    const messageContainer = document.getElementById('message-container');
    messageContainer.appendChild(htmlDivMessage);
    messageContainer.scrollTop = messageContainer.scrollHeight;
}

function onTurnStart(turnStartEvent) {
    removePlayersOutline();
    const playerToGo = turnStartEvent.player_id;
    outlinePlayer(playerToGo);
    if (getThisPlayerId() === playerToGo) {
        renderThrowDiceButton();
    }
}

function processPlayerMessage() {
    const playerMessageInput = document.getElementById('player-message-input');
    if (playerMessageInput) {
        const text = playerMessageInput.value;
        if (text.trim() !== '' && webSocket != null) {
            const playerMessage = {playerId: getThisPlayerId(), message: text};
            webSocket.send(JSON.stringify(playerMessage));
            playerMessageInput.value = '';
        }
    }
}

function outlinePlayer(playerId) {
    const playerIndex = getPlayerIndexById(playerId);
    const runningCircle = document.createElement('div');
    runningCircle.className = RUNNING_CIRCLE_OUTLINE_CLASSNAME;
    runningCircle.id = RUNNING_CIRCLE_OUTLINE_ID;
    document.getElementById(`player${playerIndex}-icon`).appendChild(runningCircle);
}

function removePlayersOutline() {
    const runningCircle = document.getElementById(RUNNING_CIRCLE_OUTLINE_ID);
    if (runningCircle) {
        runningCircle.remove();
    }
}

function onDiceStartRolling(diceRollingStartEvent) {
    renderDiceGifs();
    if (getThisPlayerId() === diceRollingStartEvent.player_id) {
        setTimeout(() => {
            sendGetHttpRequest(`${getBaseGameUrl()}/dice/result`, true)
        }, 1500);
    }
}

function onDiceResult(diceResultEvent) {
    renderDiceResult(diceResultEvent.first_dice, diceResultEvent.second_dice);
    setTimeout(() => {
        hideDice();
        if (getThisPlayerId() === diceResultEvent.player_id) {
            sendGetHttpRequest(`${getBaseGameUrl()}/dice/after`, true);
        }
    }, 2000);
}

function onPlayerChipMove(chipMoveEvent) {
    const playerId = chipMoveEvent.player_id;
    movePlayerChip(playerId, chipMoveEvent.field);
    if (chipMoveEvent.need_after_move_call && getThisPlayerId() === playerId) {
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
    const playerId = buyProposalEvent.player_id;
    const price = buyProposalEvent.price;
    const fieldName = buyProposalEvent.field_name;
    if (getThisPlayerId() === playerId) {
        removeOldActionContainer();
        let acceptButton = createActionButton('Buy', `${getBaseGameUrl()}/buy?action=ACCEPT`, !buyProposalEvent.payable);
        let auctionButton = createActionButton('Auction', `${getBaseGameUrl()}/buy?action=DECLINE`, false);
        renderActionContainer(`Do you like to buy ${fieldName} for $${price}?`, acceptButton, auctionButton);
    }
}

function onJailReleaseProcess(jailReleaseProcessEvent) {
    removePlayersOutline();
    const imprisonedPlayerId = jailReleaseProcessEvent.player_id;
    outlinePlayer(imprisonedPlayerId);
    if (getThisPlayerId() === imprisonedPlayerId) {
        removeOldActionContainer();
        const payButton = createActionButton(`Pay $${jailReleaseProcessEvent.bail}`, `${getBaseGameUrl()}/jail?action=PAY`,
            !jailReleaseProcessEvent.bail_available);
        const luckButton = createActionButton('Try luck', `${getBaseGameUrl()}/jail?action=LUCK`, false);
        renderActionContainer('Chose a way out:', payButton, luckButton);
    }
}

function onAuctionBuyProposal(auctionBuyProposalEvent) {
    const buyButton = createActionButton('Buy', `${getBaseGameUrl()}/auction/buy?action=ACCEPT`, false);
    const declineButton = createActionButton('Decline', `${getBaseGameUrl()}/auction/buy?action=DECLINE`, false);
    renderActionContainer(
        `Do you want to buy ${auctionBuyProposalEvent.field_name} for $${auctionBuyProposalEvent.proposal}?`,
        buyButton, declineButton);
}

function onAuctionRaiseProposal(auctionRaiseProposalEvent) {
    const raiseButton = createActionButton('Raise', `${getBaseGameUrl()}/auction/raise?action=ACCEPT`, false);
    const declineButton = createActionButton('Decline', `${getBaseGameUrl()}/auction/raise?action=DECLINE`, false);
    renderActionContainer(
        `Do you want to raise ${auctionRaiseProposalEvent.field_name} price to $${auctionRaiseProposalEvent.proposal}?`,
        raiseButton, declineButton);
}

function onPayCommand(payCommandEvent) {
    removeOldActionContainer();
    const sum = payCommandEvent.sum;
    const payable = payCommandEvent.payable;
    const wiseToGiveUp = payCommandEvent.wise_to_give_up;
    const payButton = createActionButton('Pay', `${getBaseGameUrl()}/pay`, !payable);
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = createActionButton('Give up');
        addClickEvent(giveUpButton, () => renderGiveUpConfirmation());
    }
    renderActionContainer(`Pay $${sum}`, payButton, giveUpButton);
}

function onMortgageChange(mortgageChangeEvent) {
    const changes = mortgageChangeEvent.changes;
    for (let change of changes) {
        renderMortgageState(change.field, change.turns);
    }
}

function onGameOver(gameOverEvent) {
    const winnerName = gameOverEvent.player_name;

    const winnerInfoContainer = document.createElement('div');
    winnerInfoContainer.className = 'fullscreen-shadow-container';
    winnerInfoContainer.id = 'winner-info-container'

    const winnerInfo = document.createElement('div');
    winnerInfo.className = 'center-screen-container';
    winnerInfo.innerText = `${winnerName} is the winner!`;

    winnerInfoContainer.appendChild(winnerInfo);
    document.body.appendChild(winnerInfoContainer);

    reloadPageOnGameOver();
}

function reloadPageOnGameOver() {
    setTimeout(() => {
        const winnerInfoContainer = document.getElementById('winner-info-container');
        if (winnerInfoContainer) {
            winnerInfoContainer.remove();
        }
        if (webSocket != null && webSocket.readyState !== WebSocket.OPEN) {
            webSocket.close();
            webSocket = null;
        }
        location.reload();
    }, 5000);
}

function applyFieldManagementEvents(fieldIndex) {
    const htmlField = document.getElementById(`field${fieldIndex}`);
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

function onPlayerBankrupt(socketMessage) {
    const bankruptPlayerId = socketMessage.player_id;
    bankruptPlayer(bankruptPlayerId);
    if (getThisPlayerId() === bankruptPlayerId) {
        removeOldActionContainer();
    }
}

function getThisPlayerId() {
    if (typeof thisPlayerId === undefined || thisPlayerId === null) {
        thisPlayerId = getPlayerIdFromCookie();
    }
    return thisPlayerId;
}

function initialStartPageBackgroundSet() {
    let backgroundImage = document.getElementById('startPageBackgroundImage');
    startBackgroundImageWidth = backgroundImage.width;
    startBackgroundImageHeight = backgroundImage.height;
    backgroundImage.remove();
    resizeBackgroundImage();
}

function resizeBackgroundImage() {
    let backgroundImageDiv = document.getElementById('backgroundImageDiv');
    let windowWidth = window.innerWidth;
    let windowHeight = window.innerHeight;
    let proportion = Math.max(windowWidth/startBackgroundImageWidth, windowHeight/startBackgroundImageHeight);

    backgroundImageDiv.style.backgroundSize =
        `${Math.ceil(proportion * startBackgroundImageWidth)}px ${Math.ceil(proportion * startBackgroundImageHeight)}px`;
    backgroundImageDiv.style.display = 'block';
}

function hideStartElements() {
    let playersBeforeGamePage = document.getElementById('playersBeforeGame');
    if (playersBeforeGamePage) {
        playersBeforeGamePage.remove();
    }
    let backgroundImageDiv = document.getElementById('backgroundImageDiv');
    if (backgroundImageDiv) {
        backgroundImageDiv.remove();
    }
    window.removeEventListener('resize', resizeBackgroundImage);
}