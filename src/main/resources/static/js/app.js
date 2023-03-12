'strict mode';

import * as Buttons from './buttons.js';
import * as Dice from './dice.js';
import * as HttpUtils from './http.js';
import * as MapFields from './field-view.js';
import * as PlayerService from './players.js';
import * as Utils from './utils.js';
import * as Offers from './offer.js';
import * as GameRoom from './game-room.js';
import * as StartPage from './start-page.js';
import * as Background from './start-background.js';
import {initialiseChipParams} from './chip-movement.js';
import {displayAtopMapMessage, hideAtopMapMessage} from "./game-map.js";

const PLAYER_ID_COOKIE = 'player_id';
const RUNNING_CIRCLE_OUTLINE_CLASSNAME = 'running-circle';
const RUNNING_CIRCLE_OUTLINE_ID = 'running-circle';

let thisPlayerId = null;
let webSocket = null;
let gameInProgress = false;
let firstMapRefresh = true;

let _MAIN_CONTAINER = null;

window.onload = () => {
    const hostInfoHolder = document.getElementById('proxy-host');
    const host = hostInfoHolder.innerText;
    hostInfoHolder.remove();
    HttpUtils.setHost(host);

    preloadImagesAndInfo();
    prepareMainPage();

    /*const reconnect = document.getElementById('reconnect');
    if (reconnect) {
        if (webSocket == null || webSocket.readyState === WebSocket.CLOSED) {
            getStartPageElement().style.display = 'none';
            openWebsocket(reconnect.innerText);
        } else {
            console.warn('websocket is not closed!')
        }
        return;
    }
     */
};

function prepareMainPage() {
    const mainContainer = getMainContainer();

    // TODO: call BE endpoint to check game status for a player, reconnect or show start page
    if (true) {
        renderStartPage();
    } else {
        reconnectToGame();
    }
}

function joinGameRoom() {
    const playerName = StartPage.getPlayerNameFromInput();
    HttpUtils.get(`${HttpUtils.baseGameUrl()}?name=${playerName}`,
        () => {
            closeStartPage();
            renderGameRoomPage();
            openWebsocket(playerName);
        });
}

function openWebsocket(username) {
    webSocket = new WebSocket(`${HttpUtils.getBaseWebsocketUrl()}/${username}`);
    webSocket.onclose = (closeEvent) => {
        console.debug('websocket connection is closed');
        const eventCode = closeEvent.code;
        if (eventCode === 3000) {
            returnToStartPageWithDelay();
        } else if (eventCode === 1000) {
            // TODO: check in-game logic
        } else {
            location.reload();
        }
    };
    webSocket.onmessage = (message) => {
        const socketMessage = JSON.parse(message.data);
        const socketMessageCode = socketMessage.code;
        console.debug(`websocket event code is ${socketMessageCode}`);
        if (socketMessageCode === 101) {
            GameRoom.addToGameRoom(socketMessage.player_name);
        } else if (socketMessageCode === 102) {
            GameRoom.removeFromGameRoom(socketMessage.player_name);
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
            MapFields.renderFieldViews(socketMessage.changes);
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
            MapFields.renderHouses(socketMessage.field, socketMessage.amount);
        } else if (socketMessageCode === 315) {
            onGameOver(socketMessage);
        } else if (socketMessageCode === 316) {
            Offers.renderOfferProposal(socketMessage);
        } else if (socketMessageCode === 317) {
            Offers.removeReplyWaitingScreen();
        } else if (socketMessageCode === 318) {
            Offers.renderReplyWaitingScreen();
        }
    };
}

function disconnectPlayer() {
    if (webSocket !== null) {
        // custom code to parse on server side
        webSocket.close(1000, 'GOING_AWAY');
    }
    webSocket = null;
}

function getPlayerIdFromCookie() {
    const playerCookie = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${PLAYER_ID_COOKIE}=`));
    return playerCookie ? playerCookie.split('=')[1] : null;
}

function startGame() {
    HttpUtils.post(HttpUtils.baseGameUrl(), null, () => {
        gameInProgress = true;
        closeGameRoomPage();
        if (Background.isVisible()) {
            Background.hide();
        }
    });
}

function onGameStartOrMapRefresh(gameMapRefreshEvent) {
    if (firstMapRefresh) {
        // first game map refresh
        firstMapRefresh = false;
        GameRoom.clear();
        getMainContainer().innerHTML = '';
    }
    // TODO: render game map html content

    document.getElementById('map').style.display = 'block';
    document.body.style.backgroundColor = 'darkslategray';

    // TODO: resize image and make grid fit it
    document.getElementById('mapTable').style.backgroundImage = "url('/images/map-back.png')";
    document.getElementById('mapTable').style.backgroundSize = '658px';

    const players = gameMapRefreshEvent.players;
    PlayerService.addPlayers(players);

    outlinePlayer(gameMapRefreshEvent.current_player);

    const fieldViews = gameMapRefreshEvent.fields;
    MapFields.renderFieldViews(fieldViews);
    for (let fieldView of fieldViews) {
        applyFieldManagementEvents(fieldView.id);
    }
    Buttons.configureMessageSendButton(webSocket, getThisPlayerId());
}

function onChatMessage(chatMessageEvent) {
    const playerId = chatMessageEvent.player_id;
    const messageElement = messageDiv();

    const nameText = document.createElement('span');
    nameText.style.color = PlayerService.getPlayerColorById(playerId);
    nameText.style.fontWeight = 'bold';
    nameText.innerText = PlayerService.getPlayerNameById(playerId);

    const messageText = document.createElement('span');
    messageText.textContent = `: ${chatMessageEvent.message}`;

    messageElement.append(nameText, messageText);
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
        Buttons.renderThrowDiceButton();
    }
}

function outlinePlayer(playerId) {
    const playerIndex = PlayerService.getPlayerIndexById(playerId);
    const runningCircle = document.createElement('div');
    runningCircle.className = RUNNING_CIRCLE_OUTLINE_CLASSNAME;
    runningCircle.id = RUNNING_CIRCLE_OUTLINE_ID;
    document.getElementById(`player${playerIndex}-icon`).appendChild(runningCircle);
}

function removePlayersOutline() {
    Utils.removeElementsIfPresent(RUNNING_CIRCLE_OUTLINE_ID);
}

function onDiceStartRolling(diceRollingStartEvent) {
    Dice.renderRollingDice();
    if (getThisPlayerId() === diceRollingStartEvent.player_id) {
        setTimeout(
            () => HttpUtils.get(`${HttpUtils.baseGameUrl()}/dice/result`),
            1500);
    }
}

function onDiceResult(diceResultEvent) {
    Dice.renderDiceResult(diceResultEvent.first_dice, diceResultEvent.second_dice);
    setTimeout(
        () => {
            Dice.hideDice();
            if (getThisPlayerId() === diceResultEvent.player_id) {
                HttpUtils.get(`${HttpUtils.baseGameUrl()}/dice/after`);
            }
        },
        2000);
}

function onPlayerChipMove(chipMoveEvent) {
    const playerId = chipMoveEvent.player_id;
    PlayerService.movePlayerChip(playerId, chipMoveEvent.field);
    if (chipMoveEvent.need_after_move_call && getThisPlayerId() === playerId) {
        setTimeout(
            () => HttpUtils.get(`${HttpUtils.baseGameUrl()}/after_move`),
            500);
    }
}

function onMoneyChange(moneyChangeEvent) {
    for (let change of moneyChangeEvent.changes) {
        PlayerService.changePlayerMoney(change.player_id, change.money);
    }
}

function onBuyProposal(buyProposalEvent) {
    const playerId = buyProposalEvent.player_id;
    const price = buyProposalEvent.price;
    const fieldName = buyProposalEvent.field_name;
    if (getThisPlayerId() === playerId) {
        Buttons.removeOldActionContainer();
        let acceptButton = Buttons.createActionButton('Buy', `${HttpUtils.baseGameUrl()}/buy?action=ACCEPT`, !buyProposalEvent.payable);
        let auctionButton = Buttons.createActionButton('Auction', `${HttpUtils.baseGameUrl()}/buy?action=DECLINE`, false);
        Buttons.renderActionContainer(`Do you like to buy ${fieldName} for $${price}?`, acceptButton, auctionButton);
    }
}

function onJailReleaseProcess(jailReleaseProcessEvent) {
    removePlayersOutline();
    const imprisonedPlayerId = jailReleaseProcessEvent.player_id;
    outlinePlayer(imprisonedPlayerId);
    if (getThisPlayerId() === imprisonedPlayerId) {
        Buttons.removeOldActionContainer();
        const payButton = Buttons.createActionButton(`Pay $${jailReleaseProcessEvent.bail}`,
            `${HttpUtils.baseGameUrl()}/jail?action=PAY`,
            !jailReleaseProcessEvent.bail_available);
        const luckButton = Buttons.createActionButton('Try luck',
            `${HttpUtils.baseGameUrl()}/jail?action=LUCK`, false);
        Buttons.renderActionContainer('Chose a way out:', payButton, luckButton);
    }
}

function onAuctionBuyProposal(auctionBuyProposalEvent) {
    const buyButton = Buttons.createActionButton('Buy',
        `${HttpUtils.baseGameUrl()}/auction/buy?action=ACCEPT`, false);
    const declineButton = Buttons.createActionButton('Decline',
        `${HttpUtils.baseGameUrl()}/auction/buy?action=DECLINE`, false);
    Buttons.renderActionContainer(
        `Do you want to buy ${auctionBuyProposalEvent.field_name} for $${auctionBuyProposalEvent.proposal}?`,
        buyButton, declineButton);
}

function onAuctionRaiseProposal(auctionRaiseProposalEvent) {
    const raiseButton = Buttons.createActionButton('Raise',
        `${HttpUtils.baseGameUrl()}/auction/raise?action=ACCEPT`, false);
    const declineButton = Buttons.createActionButton('Decline',
        `${HttpUtils.baseGameUrl()}/auction/raise?action=DECLINE`, false);
    Buttons.renderActionContainer(
        `Do you want to raise ${auctionRaiseProposalEvent.field_name} price to $${auctionRaiseProposalEvent.proposal}?`,
        raiseButton, declineButton);
}

function onPayCommand(payCommandEvent) {
    Buttons.removeOldActionContainer();
    const sum = payCommandEvent.sum;
    const payable = payCommandEvent.payable;
    const wiseToGiveUp = payCommandEvent.wise_to_give_up;
    const payButton = Buttons.createActionButton('Pay', `${HttpUtils.baseGameUrl()}/pay`, !payable);
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = Buttons.createActionButton('Give up');
        Buttons.addClickEvent(giveUpButton, Buttons.renderGiveUpConfirmation);
    }
    Buttons.renderActionContainer(`Pay $${sum}`, payButton, giveUpButton);
}

function onMortgageChange(mortgageChangeEvent) {
    const changes = mortgageChangeEvent.changes;
    for (let change of changes) {
        MapFields.renderMortgageState(change.field, change.turns);
    }
}

function onGameOver(gameOverEvent) {
    const winnerName = gameOverEvent.player_name;
    const text = `${winnerName} is the winner!`;
    displayAtopMapMessage(text);
    returnToStartPageWithDelay();
}

function returnToStartPageWithDelay() {
    setTimeout(
        () => {
            hideAtopMapMessage();
            if (webSocket != null && webSocket.readyState === WebSocket.OPEN) {
                webSocket.close();
                webSocket = null;
            }
            document.getElementById('map').style.display = 'none';
            renderStartPage();
        },
        5000);
}

function applyFieldManagementEvents(fieldIndex) {
    const htmlField = document.getElementById(`field${fieldIndex}`);
    if (!htmlField) {
        console.error(`no field with id ${fieldIndex} found on map`);
        return;
    }
    htmlField.addEventListener('click', (event) => {
        if (event.target.id.startsWith(Buttons.PROPERTY_MANAGEMENT_PREFIX)) {
            return;
        }
        HttpUtils.get(`${HttpUtils.baseGameUrl()}/field/${fieldIndex}/management`,
            (responseBody) => {
                Buttons.renderPropertyManagementContainer(htmlField, fieldIndex, responseBody);
            }
        )
    });
}

function onPlayerBankrupt(socketMessage) {
    const bankruptPlayerId = socketMessage.player_id;
    PlayerService.bankruptPlayer(bankruptPlayerId);
    if (getThisPlayerId() === bankruptPlayerId) {
        Buttons.removeOldActionContainer();
    }
}

function getThisPlayerId() {
    if (thisPlayerId === null) {
        thisPlayerId = getPlayerIdFromCookie();
    }
    return thisPlayerId;
}

function preloadImagesAndInfo() {
    Promise.allSettled([
        imagePreload('images/map-back.png', 'images/loading-bubbles.gif'),
        initialiseChipParams(),
        Dice.preloadDice()
    ])
        .catch(error => console.log('failed to load some resources or data: ' + error));
}

async function imagePreload() {
    let amount = arguments.length;
    for (let i = 0; i < amount; i++) {
        new Image().src = arguments[i];
    }
}

function getMainContainer() {
    if (_MAIN_CONTAINER === null) {
        _MAIN_CONTAINER = document.getElementById('mainContainer');
    }
    return _MAIN_CONTAINER;
}

function renderStartPage() {
    ensureBackgroundIsVisible();
    const mainContainer = getMainContainer();
    const startPage = StartPage.getStartPageElement();
    mainContainer.innerHTML = '';
    mainContainer.appendChild(startPage);
    StartPage.getPlayerNameInput().focus();
    const submitPlayerNameButton = StartPage.getSubmitPlayerNameButton();
    Buttons.addClickEvent(submitPlayerNameButton, joinGameRoom);
    window.addEventListener('keypress', autoSubmitPlayerNameOnEnterPress);
}

function closeStartPage() {
    StartPage.getSubmitPlayerNameButton().removeEventListener('click', joinGameRoom);
    window.removeEventListener('keypress', autoSubmitPlayerNameOnEnterPress);
}

function autoSubmitPlayerNameOnEnterPress(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        StartPage.getSubmitPlayerNameButton().click();
    }
}

function renderGameRoomPage() {
    ensureBackgroundIsVisible();
    const mainContainer = getMainContainer();
    const gameRoomPage = GameRoom.getGameRoomPageElement();
    mainContainer.innerHTML = '';
    mainContainer.appendChild(gameRoomPage);
    Buttons.addClickEvent(GameRoom.getStartGameButton(), startGame);
    Buttons.addClickEvent(GameRoom.getLeaveGameRoomButton(), leaveGameRoom);
}

function closeGameRoomPage() {
    GameRoom.clear();
    GameRoom.getStartGameButton().removeEventListener('click', startGame);
    GameRoom.getLeaveGameRoomButton().removeEventListener('click', leaveGameRoom);
}

function leaveGameRoom() {
    disconnectPlayer();
    closeGameRoomPage();
    renderStartPage();
}

function ensureBackgroundIsVisible() {
    if (!Background.isRendered()) {
        Background.renderBackground(document.body);
    }
    if (Background.isRendered() && !Background.isVisible()) {
        Background.show();
    }
}