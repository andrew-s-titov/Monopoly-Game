import {PLAYER_COLORS} from './colors.js'
import {moveChip} from "./chip-movement.js";
import {addClickEvent, renderGiveUpConfirmation} from "./buttons.js";
import {getBaseGameUrl, sendGetHttpRequest} from "./http.js";

const PLAYER_MAP = new Map();
const CHIP_MAP = new Map();

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
        } else {
            document.getElementById(`player${playerObject.index}-money`).style.color = 'grey';
            document.getElementById(`player${playerObject.index}-name`).style.color = 'grey';
        }
    }
}

export function bankruptPlayer(playerId) {
    let playerIndex = PLAYER_MAP.get(playerId).index;

    document.getElementById(`player${playerIndex}-group`).style.backgroundColor = 'transparent';

    let playerMoneyField = document.getElementById(`player${playerIndex}-money`);
    playerMoneyField.style.color = 'grey';

    let playerNameField = document.getElementById(`player${playerIndex}-name`);
    playerNameField.style.color = 'grey';

    CHIP_MAP.get(playerIndex).remove();
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
    chip.className = 'chip-outer';
    let chipInnerCircle = document.createElement('div');
    chipInnerCircle.className = 'chip-inner';
    chipInnerCircle.style.background = color;

    chip.appendChild(chipInnerCircle);
    CHIP_MAP.set(index, chip);
    document.getElementById('mapTable').appendChild(chip);
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
            button.innerHTML = 'Give up';
            addClickEvent(button, () => renderGiveUpConfirmation());
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