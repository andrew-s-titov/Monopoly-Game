const PLAYER_COLORS = ['cornflowerblue', 'crimson', 'mediumseagreen', 'purple', 'orange'];
const GROUP_COLORS = ['pink', 'lightcoral', 'gold', 'darkcyan', 'firebrick',
    'blue', 'greenyellow', 'darkturquoise', 'mediumpurple', 'slategrey'];
const PLAYER_ID_COOKIE = 'player_id';
const THROW_DICE_BUTTON_ID = 'throwDiceButton';
const DICE_CONTAINER_ID = 'diceContainer';
const PROPOSAL_CONTAINER_ID = 'proposalContainer';

let thisPlayerId = null;
let thisPlayerName = null;
let webSocket = null;
let gameInProgress = false;
let playerToGo = null;
let playersMap = new Map();
let chips = new Map();

const priceSpacePx = 13;
const wideSidePx = 90;
const narrowSidePx = 50;
const stepPx = 50;
const chipSizePx = 20;
const mapLeftMarginPx = 300;
const cornerStepAdjustmentPx = (wideSidePx - narrowSidePx) / 2;
const chipWidthAdjustment = chipSizePx / 2;

let playerIconWidthPx = 150;

window.addEventListener('load', (event) => {
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
})

function joinToRoom() {
    const playerName = document.getElementById('playerNameInput').value;

    let httpRequester = new XMLHttpRequest();
    httpRequester.onload = () => {
        if (httpRequester.readyState === 4) {
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
            onChipMove(socketMessage);
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
    return playerCookie.split('=')[1];
}

function startGame() {
    gameInProgress = true;
    let httpRequester = new XMLHttpRequest();
    httpRequester.onload = () => {
        if (httpRequester.readyState === 4) {
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

    if (thisPlayerId == null) {
        thisPlayerId = getPlayerIdFromCookie();
    } else {
        savePlayerIdToCookie(thisPlayerId);
    }

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
        playerIcon.setAttribute('width', '90px');
        let playerIconField = document.getElementById('player' + i + '-icon');
        playerIconField.appendChild(playerIcon);
        playerIconField.style.backgroundColor = playerColor;

        // adding to map server player ID with array ID
        playersMap.set(player.id, [i, player.name]);

        let chip = document.createElement('div');
        chip.id = 'chip' + i;
        chip.className = 'chip';
        chip.style.background = PLAYER_COLORS[i];
        chip.style.opacity = '0.8'
        let playerPosition = player.position;
        document.getElementById('map').appendChild(chip);
        chips.set(player.id, chip);
        defineChipNewPosition(player.id, playerPosition);
    }

    playerToGo = socketMessage.current_player;
    outlinePlayer(playerToGo);

    renderFieldViews(socketMessage.fields);

    // auto-click 'send' button in message box if input is active
    let playerMessageInput = document.getElementById('playerMessage');
    playerMessageInput.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            event.preventDefault();
            document.getElementById('messageButton').click();
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
    playerToGo = socketMessage.player_id;
    outlinePlayer(playerToGo);
    if (thisPlayerId === playerToGo) {
        let throwDiceButton = renderDiceButton();
        throwDiceButton.addEventListener('click', () => {
            throwDiceButton.remove();
            let httpRequester = new XMLHttpRequest();
            httpRequester.open('GET', 'http://localhost:8080/game/dice/notify', true);
            httpRequester.send();
        });
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

function renderDiceButton() {
    let throwDiceButton = document.createElement('button');
    throwDiceButton.setAttribute('id', THROW_DICE_BUTTON_ID);
    throwDiceButton.style.position = 'fixed';
    throwDiceButton.style.left = '50%';
    throwDiceButton.style.top = '50%';
    throwDiceButton.style.display = 'block'
    throwDiceButton.innerText = 'Roll the dice!';
    document.getElementById('map').appendChild(throwDiceButton);
    return throwDiceButton;
}

function renderDiceGifs() {
    let diceContainer = document.createElement('div');
    diceContainer.id = DICE_CONTAINER_ID;
    diceContainer.style.position = 'fixed';
    diceContainer.style.left = '47%';
    diceContainer.style.top = '50%';

    let diceLeft = document.createElement('img');
    diceLeft.id = 'diceLeft';
    diceLeft.src = 'images/dice-left.gif';
    diceLeft.style.float = 'left';

    let diceRight = document.createElement('img');
    diceRight.id = 'diceRight';
    diceRight.src = 'images/dice-right.gif';
    diceRight.style.float = 'left';

    diceContainer.appendChild(diceLeft);
    diceContainer.appendChild(diceRight);

    document.getElementById('map').appendChild(diceContainer);
}

function diceGifToValues(left, right) {
    document.getElementById('diceLeft').src = 'images/dice' + left + '.png';
    document.getElementById('diceRight').src = 'images/dice' + right + '.png';
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

function onChipMove(socketMessage) {
    defineChipNewPosition(socketMessage.player_id, socketMessage.field);
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
        let acceptButton = createActionButton('Buy', 'http://localhost:8080/game/buy?action=ACCEPT');
        let auctionButton = createActionButton('Auction', 'http://localhost:8080/game/buy?action=DECLINE');
        createActionContainer('Do you like to buy ' + field + ' for $' + price + '?', acceptButton, auctionButton);
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
    outlinePlayer(thisPlayerId);
    let payButton = createActionButton('Pay $ ' + socketMessage.bail, 'http://localhost:8080/game/jail?action=PAY');
    let luckButton = createActionButton('Try luck', 'http://localhost:8080/game/jail?action=LUCK');
    createActionContainer('Chose a way out:', payButton, luckButton);
}

function onAuctionBuyProposal(socketMessage) {
    let buyButton = createActionButton('Buy', 'http://localhost:8080/game/auction/buy?action=ACCEPT');
    let declineButton = createActionButton('Decline', 'http://localhost:8080/game/auction/buy?action=DECLINE');
    createActionContainer(
        'Do you want to buy ' + socketMessage.field_name + ' for ' + socketMessage.proposal + ' ?',
        buyButton, declineButton);
}

function onAuctionRaiseProposal(socketMessage) {
    let raiseButton = createActionButton('Raise', 'http://localhost:8080/game/auction/raise?action=ACCEPT');
    let declineButton = createActionButton('Decline', 'http://localhost:8080/game/auction/raise?action=DECLINE');
    createActionContainer(
        'Do you want to raise ' + socketMessage.field_name + ' price to $' + socketMessage.proposal + '?',
        raiseButton, declineButton);
}

function onFieldViewChange(socketMessage) {
    renderFieldViews(socketMessage.changes);
}

function onPayCommand(socketMessage) {
    let oldContainer = document.getElementById(PROPOSAL_CONTAINER_ID);
    if (oldContainer) {
        oldContainer.remove();
    }
    let sum = socketMessage.sum;
    let payable = socketMessage.payable;
    let wiseToGiveUp = socketMessage.wise_to_give_up;
    let payButton = createActionButton('Pay', 'http://localhost:8080/game/pay');
    payButton.disabled = !payable;
    let giveUpButton = null;
    if (wiseToGiveUp) {
        giveUpButton = createActionButton('Give up', 'http://localhost:8080/game/give_up');
    }
    createActionContainer('Pay $' + sum, payButton, giveUpButton);
}

function hideDice() {
    document.getElementById(DICE_CONTAINER_ID).remove();
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

function defineChipNewPosition(playerId, fieldNumber) {
    let chip = chips.get(playerId);
    chip.style.top = defineChipTop(fieldNumber);
    chip.style.left = defineChipLeft(fieldNumber);
}

// returning string for 'style.top'
function defineChipTop(fieldNumber) {
    let startTop = priceSpacePx + wideSidePx / 2 + 8; // adding body default margin;
    let postfix = 'px';
    if (fieldNumber >= 0 && fieldNumber <= 10) {
        return startTop + postfix;
    } else if (fieldNumber >= 20 && fieldNumber <= 30) {
        return (startTop
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldNumber > 10 && fieldNumber < 20) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (fieldNumber - 10))
            + postfix;
    } else if (fieldNumber > 30 && fieldNumber < 40) {
        return (startTop
                + cornerStepAdjustmentPx
                + stepPx * (40 - fieldNumber))
            + postfix;
    } else {
        console.error('field number exceeds map size');
    }
}

// returning string for 'style.left'
function defineChipLeft(fieldNumber) {
    let startLeft = 8 + mapLeftMarginPx + playerIconWidthPx + priceSpacePx + wideSidePx / 2;
    let postfix = 'px';
    if (fieldNumber === 0 || (fieldNumber >= 30 && fieldNumber < 40)) {
        return startLeft + postfix;
    } else if (fieldNumber >= 10 && fieldNumber <= 20) {
        return (startLeft
                + stepPx * 10
                + cornerStepAdjustmentPx * 2)
            + postfix;
    } else if (fieldNumber > 0 && fieldNumber < 10) {
        return (startLeft
                + stepPx * fieldNumber
                + cornerStepAdjustmentPx)
            + postfix;
    } else if (fieldNumber > 20 && fieldNumber < 30) {
        return (startLeft
                + stepPx * (30 - fieldNumber)
                + cornerStepAdjustmentPx)
            + postfix;
    } else {
        console.error('field number exceeds map size');
    }
}

function createActionContainer(text, button1, button2) {
    let proposalContainer = document.createElement('div');
    proposalContainer.id = PROPOSAL_CONTAINER_ID;
    proposalContainer.style.width = '20%';
    proposalContainer.style.opacity = '0.8'
    proposalContainer.style.position = 'fixed';
    proposalContainer.style.left = '45%';
    proposalContainer.style.top = '35%';
    proposalContainer.style.backgroundColor = 'black';

    let proposalPhrase = document.createElement('div');
    proposalPhrase.innerText = text;
    proposalPhrase.style.fontSize = '15px';
    proposalPhrase.style.color = 'white';
    proposalPhrase.style.textAlign = 'center';
    proposalPhrase.style.marginTop = '20px';
    proposalContainer.appendChild(proposalPhrase);
    if (button1 != null) {
        button1.addEventListener('click', () => proposalContainer.remove());
        button1.style.marginBottom = '10px';
        proposalContainer.appendChild(button1);
    }
    if (button2 != null) {
        button2.addEventListener('click', () => proposalContainer.remove());
        button2.style.marginBottom = '10px';
        proposalContainer.appendChild(button2);
    }
    document.getElementById('map').appendChild(proposalContainer);
}

function createActionButton(text, url) {
    let proposalButton = document.createElement('button');
    proposalButton.innerText = text;
    proposalButton.style.display = 'block';
    proposalButton.style.width = '150px';
    proposalButton.style.margin = 'auto';
    proposalButton.style.marginTop = '20px';
    proposalButton.addEventListener('click', () => {
        let httpRequester = new XMLHttpRequest();
        httpRequester.open('GET', url, true);
        httpRequester.send();
    });
    return proposalButton;
}

function renderFieldViews(fieldViews) {
    for (let fieldView of fieldViews) {
        let fieldId = fieldView.id;
        let htmlField = document.getElementById('field' + fieldId);
        htmlField.innerHTML = fieldView.name;
        if (fieldView.hasOwnProperty('owner_id')) {
            htmlField.style.backgroundColor = PLAYER_COLORS[playersMap.get(fieldView.owner_id)[0]];
        } else {
            htmlField.style.backgroundColor = 'white';
        }
        if (fieldView.hasOwnProperty('price_tag')) {
            let priceTagField = document.getElementById('field' + fieldId + '-price');
            priceTagField.innerHTML = fieldView.price_tag;
            priceTagField.style.backgroundColor = GROUP_COLORS[fieldView.group];
        }
    }
}