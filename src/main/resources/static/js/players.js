import {PLAYER_COLORS} from './colors.js'
import {moveChip} from "./chip-movement.js";
import {addClickEvent} from "./buttons.js";
import {getBaseGameUrl, sendGetHttpRequest} from "./http.js";

const PLAYER_MAP = new Map();
const CHIP_MAP = new Map();

const PLAYER_ICON_BUTTON_ID_POSTFIX = "-button";

export function addPlayers(jsonPlayerArray) {
    for (let index = 0; index < jsonPlayerArray.length; index++) {
        let player = jsonPlayerArray[index];
        let playerColor = PLAYER_COLORS[index];
        let playerObject = new Player(index, player.name, PLAYER_COLORS[index]);
        PLAYER_MAP.set(player.id, playerObject);

        document.getElementById(`player${index}-name`).innerHTML = player.name;
        document.getElementById(`player${index}-money`).innerHTML = `$ ${player.money}`;
        renderPlayerPicture(index);
        if (player.hasOwnProperty('bankrupt') && !player.bankrupt) {
            renderPlayerChip(index, playerColor, player.position);
            document.getElementById(`player${index}-group`).style.backgroundColor = playerColor;
            document.getElementById(`player${index}-money`).style.backgroundColor = 'transparent';
            document.getElementById(`player${index}-name`).style.backgroundColor = 'transparent';
            document.getElementById(`player${index}-icon`).style.backgroundColor = 'transparent';
        } else {
            document.getElementById(`player${playerObject.index}-money`).style.color = 'grey';
            document.getElementById(`player${playerObject.index}-name`).style.color = 'grey';
        }
    }
}

export function bankruptPlayer(playerId) {
    let playerObject = PLAYER_MAP.get(playerId);
    playerObject.money = 0;

    let playerMoneyField = document.getElementById(`player${playerObject.index}-group`);
    playerMoneyField.style.backgroundColor = 'transparent';

    let playerNameField = document.getElementById(`player${playerObject.index}-name`);
    playerNameField.style.color = 'grey';

    CHIP_MAP.get(playerObject.index).remove();
}

export function changePlayerMoney(playerId, money) {
    let playerObject = PLAYER_MAP.get(playerId);
    playerObject.money = money;
    document.getElementById(`player${playerObject.index}-money`).innerHTML = `$ ${money}`;
}

export function getPlayerIndex(playerId) {
    return PLAYER_MAP.get(playerId).index;
}

export function getPlayerColor(playerId) {
    return PLAYER_MAP.get(playerId).color;
}

export function getPlayerName(playerId) {
    return PLAYER_MAP.get(playerId).name;
}

export function movePlayerChip(playerId, fieldIndex) {
    let playerIndex = PLAYER_MAP.get(playerId).index;
    let chip = CHIP_MAP.get(playerIndex);
    moveChip(chip, fieldIndex);
}

function getPlayerIdByIndex(playerIndex) {
    for (let [id, playerObject] of PLAYER_MAP) {
        if (playerObject.index === playerIndex) return id;
    }
}

function renderPlayerPicture(index) {
    let playerIcon = document.createElement('img');
    playerIcon.setAttribute('src', 'images/user.png')
    playerIcon.style.width = '90px';
    playerIcon.style.height = '90px';
    let playerIconField = document.getElementById(`player${index}-icon`);
    playerIconField.appendChild(playerIcon);

    applyPlayerManagementEvents(index);
}

function renderPlayerChip(index, color, position) {
    let chip = document.createElement('div');
    chip.id = `chip${index}`;
    chip.style.width = '24px';
    chip.style.height = '24px';
    chip.style.borderRadius = '15px';
    chip.style.position = 'fixed';
    chip.style.boxShadow = '1px 1px 1px 0.8px black';
    chip.style.transition = 'left 0.4s, top 0.4s, right 0.4s, down 0.4s';
    chip.style.transitionTimingFunction = 'linear';
    chip.style.background = 'grey';
    chip.style.opacity = '0.8';

    let chipInnerCircle = document.createElement('div');
    chipInnerCircle.style.width = '20px';
    chipInnerCircle.style.height = '20px';
    chipInnerCircle.style.borderRadius = '10px';
    chipInnerCircle.style.margin = '2px';
    chipInnerCircle.style.background = color;

    chip.appendChild(chipInnerCircle);
    CHIP_MAP.set(index, chip);
    document.getElementById('map').appendChild(chip);
    moveChip(chip, position);
}

function applyPlayerManagementEvents(playerIndex) {
    let playerIconField = document.getElementById(`player${playerIndex}-icon`);
    if (!playerIconField) {
        console.error(`no field with id ${playerIndex} found on map`);
        return;
    }
    playerIconField.addEventListener('click', (event) => {
        if (event.target.id === `player${playerIndex}-action-container`) {
            return;
        }
        sendGetHttpRequest(`${getBaseGameUrl()}/player/${getPlayerIdByIndex(playerIndex)}/management`, true,
            function (requester) {
                if (requester.readyState === XMLHttpRequest.DONE && requester.status === 200) {
                    let managementActions = JSON.parse(requester.response);
                    if (managementActions.length > 0) {
                        renderPlayerManagementContainer(playerIconField, playerIndex, managementActions);
                    }
                } else {
                    console.error('failed to load available management actions');
                    console.log(requester.response);
                }
            });
    });
}

export function renderPlayerManagementContainer(htmlPlayerIconField, playerIndex, availableActions) {
    let managementContainer = document.createElement("div");
    let containerId = `player${playerIndex}-action-container`;
    managementContainer.className = 'management-container';

    document.addEventListener('click', event => {
        if (event.target.id !== containerId) {
            managementContainer.remove();
        }
    });
    for (let action of availableActions) {
        let button = document.createElement('button');
        button.className = 'manage-player-button';
        if (action === 'GIVE_UP') {
            // TODO: show info card atop of everything;
            button.innerHTML = 'Give up';
            addClickEvent(button, () => {
                let confirmationShadow = document.createElement('div');
                confirmationShadow.style.position = 'fixed';
                confirmationShadow.style.left = '0';
                confirmationShadow.style.top = '0';
                confirmationShadow.style.width = '100%';
                confirmationShadow.style.height = '100%';
                confirmationShadow.style.opacity = '0.9';
                confirmationShadow.style.backgroundColor = 'black';

                let confirmContent = document.createElement('div');
                confirmContent.style.margin = '15% auto 15% auto';
                confirmContent.style.padding = '16px';
                confirmContent.style.backgroundColor = 'white';
                confirmContent.style.textAlign = 'center';

                let confirmTextElement = document.createElement('p');
                confirmTextElement.innerText = 'Do you really want to give up?';

                let confirmGiveUpButton = document.createElement('button');
                confirmGiveUpButton.innerText = 'Give up';
                confirmGiveUpButton.style.color = 'white';
                confirmGiveUpButton.style.backgroundColor = 'red';
                confirmGiveUpButton.style.fontSize = '15px';
                addClickEvent(confirmGiveUpButton, () => {
                    confirmationShadow.remove();
                    sendGetHttpRequest(`${getBaseGameUrl()}/player/give_up`, true);
                });

                let cancelGiveUpButton = document.createElement('button');
                cancelGiveUpButton.innerText = 'Cancel';
                cancelGiveUpButton.style.color = 'black';
                cancelGiveUpButton.style.fontSize = '15px';
                addClickEvent(cancelGiveUpButton, () => {
                    confirmationShadow.remove();
                });

                confirmContent.appendChild(confirmTextElement);
                confirmContent.appendChild(confirmGiveUpButton);
                confirmContent.appendChild(cancelGiveUpButton);
                confirmationShadow.appendChild(confirmContent);
                document.body.appendChild(confirmationShadow);
            });
        } else if (action === 'CONTRACT') {
            button.innerHTML = 'Offer a contract';
            addClickEvent(button, () => alert('sorry, not implemented yet'));
            // TODO: implement
        } else {
            managementContainer.remove();
            console.error('unknown action type');
            return;
        }
        addClickEvent(button, () => managementContainer.remove());
        managementContainer.appendChild(button);
    }
    htmlPlayerIconField.appendChild(managementContainer);
}

class Player {
    index;
    name;
    color;

    constructor(index, name, color) {
        this.index = index;
        this.name = name;
        this.color = color;
    }
}